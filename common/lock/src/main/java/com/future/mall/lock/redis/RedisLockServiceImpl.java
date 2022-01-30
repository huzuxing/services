package com.future.mall.lock.redis;

import com.future.common.redis.manager.RedisArgs;
import com.future.common.redis.manager.RedisManager;
import com.future.mall.lock.AbstractLockService;
import com.future.mall.lock.LockService;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @Author huzuxing
 * @description todo
 **/
@Slf4j
public class RedisLockServiceImpl extends AbstractLockService implements LockService {

    private final ScheduledExecutorService refreshScheduler;
    private final String lockValue = UUID.randomUUID().toString();

    private final List<RedisManager> redisManagers;
    private final long timeoutMillis;
    private final SetArgs setArgs;

    private final Map<String, Holder> holder = new ConcurrentHashMap<>();

    private final int lockCount;
    private final String lockType;

    private final Map<String, Set<RedisManager>> lockSuccessRedisManagers = new ConcurrentHashMap<>();

    private final Map<String, Function<String, Boolean>> lockHandlers = Map.of(
            "single", this::singleLock,
            "cluster", this::clusterLock,
            "sentinel", this::sentinelLock
    );

    private final String refreshScript = "" +
            "if redis.call('get', KEYS[1]) == ARGV[1] then\n" +
            "    redis.call('pexpire', KEYS[1], ARGV[2])\n" +
            "    return true\n" +
            "end\n" +
            "return false";

    private final String releaseScript = "" +
            "if redis.call('get', KEYS[1]) == ARGV[1] then\n" +
            "    redis.call('del', KEYS[1])\n" +
            "    return true\n" +
            "end\n" +
            "return false";

    public RedisLockServiceImpl(RedisArgs args) {
        assert null != args;
        this.timeoutMillis = args.getTimeoutMillis();
        this.setArgs = new SetArgs().nx().px(timeoutMillis);
        assert null != args.getHosts();
        var hosts = args.getHosts();
        int size = hosts.size();
        if (size <= 0) {
            throw new IllegalArgumentException("redis hosts is null");
        }
        lockCount = size / 2 + 1;// 只要保证这个数量的redis加锁成功，则加锁成功
        refreshScheduler = Executors.newScheduledThreadPool(lockCount);
        redisManagers = new ArrayList<>(size);
        int errorConnectCount = 0;
        for (String host : hosts) {
            RedisURI uri = RedisURI.create("redis://" + host);
            RedisManager redisManager = null;
            try {
                redisManager = new RedisManager(uri, args.getPoolSize(), args.getRetryTimes(), Duration.ofMillis(this.timeoutMillis));
            } catch (Exception e) {
                if (errorConnectCount > (redisManagers.size() - lockCount)) {
                    log.error("available redis count not satisfy your strategy for lock: lock type is : {}", args.getType());
                    throw new IllegalArgumentException("available redis count not satisfy your strategy for lock");
                }
                errorConnectCount++;
                continue;
            }
            redisManagers.add(redisManager);
        }
        if (!lockHandlers.containsKey(args.getType())) {
            throw new IllegalArgumentException("no handler for type : " + args.getType());
        }
        this.lockType = args.getType();
    }
    /**
     * 目前实现的不可重入锁
     * @param lockKey
     * @return
     */
    @Override
    public boolean lock(String lockKey) {
        synchronized (holder) {
            // 不可重入
            if (holder.containsKey(lockKey)) {
                return false;
            }
            var lockHandler = lockHandlers.get(lockType);
            return lockHandler.apply(lockKey);
        }
    }

    private boolean singleLock(String lockKey) {
        var redisManager = redisManagers.get(0);
        return lockWithRedisManager(lockKey, redisManager);
    }
    private boolean lockWithRedisManager(String lockKey, RedisManager redisManager) {
        var v = redisManager.sync().set(lockKey, lockValue, setArgs);
        var success = "OK".equals(v);
        if (success) {
            if (log.isTraceEnabled()) {
                log.trace("acquire [{}:{}] lock success", lockKey, lockValue);
            }
            try {
                long period = timeoutMillis >> 2;
                var f = refreshScheduler.scheduleAtFixedRate(() -> {
                    refreshExpireTime(lockKey, lockValue, timeoutMillis, redisManager);
                }, period, period, TimeUnit.MILLISECONDS);
                holder.computeIfAbsent(lockKey, k -> {
                    List<ScheduledFuture<?>> futures = new ArrayList<>(lockCount);
                    futures.add(f);
                    return new Holder(lockValue, futures);
                });
                holder.computeIfPresent(lockKey, (lk, hd) -> {
                    hd.futures.add(f);
                    return hd;
                });
                lockSuccessRedisManagers.computeIfAbsent(lockKey, k -> {
                   Set<RedisManager> successManager = new HashSet<>(1);
                   successManager.add(redisManager);
                   return successManager;
                });
                lockSuccessRedisManagers.computeIfPresent(lockKey, (lk, sm) -> {
                   sm.add(redisManager);
                   return sm;
                });
            } catch (Exception e) {
                releaseRedis(lockKey, lockValue);
                throw e;
            }
        }
        return success;
    }

    /**
     * 集群模式下，一定数量N的实例加锁成功，则加锁成功
     * N = n/2 + 1, n为配置的redis数量，大部分情况下，前N个会加锁成功，此时用异步加锁，当前N有加锁失败的情况，对剩余的实例进行加锁，
     * 当满足 N 个实例加锁成功，则不再对后面的实例加锁。
     * 集群模式代价高，当需要一致性比较强，则可考虑
     * 亦可选择哨兵模式，代价低点
     * @param lockKey
     * @return
     */
    private boolean clusterLock(String lockKey) {
        int successLockCount = 0;
        var asyncLocks = Flux.fromIterable(redisManagers.subList(0, lockCount)).map(rm -> {
            var f = CompletableFuture.supplyAsync(() -> {
                var success = lockWithRedisManager(lockKey, rm);
                return success ? rm : null;
            });
            return f;
        }).collectList().block();
        for (var c : asyncLocks) {
            var rm = c.join();
            if (null != rm) {
                successLockCount++;
            }
        }
        int redisManageCount = redisManagers.size();
        int nextIndex = lockCount;
        while (successLockCount < lockCount) {
            if ((nextIndex > (redisManageCount - 1)) || successLockCount == lockCount) {
                break;
            }
            var rm = redisManagers.get(nextIndex++);
            var success = lockWithRedisManager(lockKey, rm);
            if (success) {
                successLockCount++;
            }
        }
        return successLockCount == lockCount;
    }

    /**
     * 哨兵模式待写，主从模式下，需要保证主从同步后再返回，WAIT 命令可实现此功能，
     * 定义LUA脚本，set 和 wait 保证原子性
     * 缺点：当发生主从切换时，会导致数据丢失
     * @param lockKey
     * @return
     */
    private boolean sentinelLock(String lockKey) {
        // todo
        return false;
    }

    private void refreshExpireTime(String lockKey, String lockValue, long expireTime, RedisManager redisManager) {
        if (log.isTraceEnabled()) {
            log.debug("refresh [{}:{}] expireInMillis={}", lockKey, lockValue, expireTime);
        }
        var future = redisManager.async().eval(refreshScript, ScriptOutputType.BOOLEAN,
                new String[]{lockKey}, lockValue, Long.toString(expireTime)).toCompletableFuture();
        if (log.isDebugEnabled()) {
            future.whenComplete((v, e) -> {
               if (null == e) {
                   log.debug("refresh [{}:{}] expire {}", lockKey, lockValue, expireTime);
               }
               else {
                   log.debug("refresh [{}:{}] expire {}, {}", lockKey, lockValue, expireTime, e);
               }
            });
        }
    }

    @Override
    public boolean release(String lockKey) {
        synchronized (holder) {
            final var hd = holder.remove(lockKey);
            if (null == hd) {
                return false;
            }
            hd.futures.forEach(future -> {
                future.cancel(false);
            });
            return releaseRedis(lockKey, hd.value);
        }
    }

    private boolean releaseRedis(String lockKey, String lockValue) {
        int successCount = 0;
        var sm = lockSuccessRedisManagers.remove(lockKey);
        if (null == sm) {
            return false;
        }
        for (var lockSuccessRedisManager : sm) {
            boolean success = lockSuccessRedisManager.sync().eval(releaseScript, ScriptOutputType.BOOLEAN, new String[]{lockKey}, lockValue);
            if (success) {
                successCount++;
            }
        }
        return successCount == sm.size();
    }

    static class Holder {
        private final String value;
        private final List<ScheduledFuture<?>> futures;
        Holder(String value, List<ScheduledFuture<?>> futures) {
            assert null != value;
            assert null != futures;
            this.value = value;
            this.futures = futures;
        }
    }
}
