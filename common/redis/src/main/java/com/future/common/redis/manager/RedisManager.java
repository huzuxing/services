package com.future.common.redis.manager;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.pool.InstrumentedPool;
import reactor.pool.PoolBuilder;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @Author huzuxing
 * @description todo lettuce api to redis :
 * 官网文档一段描述：Redis connections are designed to be long-lived and thread-safe, and if the connection is lost will reconnect until close() is called.
 * Pending commands that have not timed out will be (re)sent after successful reconnection.
 **/
@Slf4j
public class RedisManager {
    private static final ClientResources CLIENT_RESOURCES = DefaultClientResources.create();
    private static final RedisCodec<String, String> DEFAULT_CODES = StringCodec.UTF8;
    private final RedisClient redisClient;
    private final RedisURI redisURI;
    private final boolean isCluster;
    private final int poolSize;
    private final int retryTimes;
    private final Duration retryDelay;
    private final InstrumentedPool<StatefulRedisConnection<String, String>> connPool;
    private final StatefulRedisConnection<String, String> defConn;
    private volatile String stringURI = null;

    private final Map<RedisCodec<?, ?>, RedisCommands> syncMap = new ConcurrentHashMap<>();
    private final Map<RedisCodec<?, ?>, RedisAsyncCommands> asyncMap = new ConcurrentHashMap<>();
    private final Map<RedisCodec<?, ?>, RedisReactiveCommands<?, ?>> reactiveMap = new ConcurrentHashMap<>();


    static final Predicate<Throwable> RetryFilter = e -> e instanceof RedisException;

    public RedisManager(RedisURI redisURI, int poolSize, int retryTimes, Duration retryDelay) {
        this.redisURI = Objects.requireNonNull(redisURI);
        if (poolSize < 0) {
            throw new IllegalArgumentException("poolSize max must be positive or zero");
        }
        this.poolSize = poolSize;
        this.retryTimes = retryTimes;
        this.retryDelay = null == retryDelay ? Duration.ZERO : retryDelay;
        this.isCluster = !redisURI.getSentinels().isEmpty();
        this.redisClient = RedisClient.create(CLIENT_RESOURCES);
        this.defConn = connectAsync(DEFAULT_CODES).join();
        this.connPool = poolSize > 1 ? createPool(DEFAULT_CODES) : new SingletonPool<>(defConn);
    }

    private <K, V>CompletableFuture<StatefulRedisConnection<K, V>> connectAsync(RedisCodec<K, V> redisCodec) {
        if (isCluster) {
            return MasterReplica.connectAsync(redisClient, redisCodec, redisURI).thenApply(conn -> conn);
        }
        return redisClient.connectAsync(redisCodec, redisURI).toCompletableFuture();
    }

    private <K,V> InstrumentedPool<StatefulRedisConnection<K,V>> createPool(RedisCodec<K,V> redisCodec) {
        var creator = Mono.fromCompletionStage(() -> {
            log.info("connect to : {}", getUri());
            return connectAsync(redisCodec);
        });
        var builder = PoolBuilder.from(creator)
                .destroyHandler(c -> Mono.fromCompletionStage(c.closeAsync()))
                .evictionPredicate((c, md) -> !c.isOpen())
                .sizeBetween(0, poolSize);
        return builder.buildPool();
    }
    public String getUri() {
        var s = stringURI;
        if (null != s) return s;
        synchronized (redisURI) {
            s = stringURI;
            if (null != s) return s;

            var uri = redisURI;
            var sb = new StringBuilder(128);
            if (isCluster) {
                sb.append('[');
                boolean first = true;
                for (var sentinel : uri.getSentinels()) {
                    if (first) first = false;
                    else sb.append(',');
                    sb.append(sentinel.getHost()).append(':').append(sentinel.getPort());
                }
                sb.append(']');
            } else {
                sb.append(uri.getHost()).append(':').append(uri.getPort());
            }
            sb.append('/').append(uri.getDatabase());
            sb.append("?ssl=").append(uri.isSsl());
            if (uri.getTimeout() != null) {
                sb.append("&timeout=").append(uri.getTimeout().toSeconds());
            }
            if (uri.getPassword() != null) {
                sb.append("&password=").append(uri.getPassword());
            }
            stringURI = s = sb.toString();
            return s;
        }
    }

    public RedisCommands<String, String> sync() {
        return sync(DEFAULT_CODES);
    }

    private <K, V> RedisAsyncCommands<K, V> createAsync(RedisCodec<K, V> codec) {
        return new PooledRedisAsyncCommands<>(pool(), defaultConn(), codec,
                retryTimes, retryDelay, RetryFilter);
    }

    private <K, V> InstrumentedPool<StatefulRedisConnection<K, V>> pool() {
        return (InstrumentedPool<StatefulRedisConnection<K, V>>) (InstrumentedPool) connPool;
    }

    public <K, V> RedisAsyncCommands<K, V> async(RedisCodec<K, V> redisCodec) {
        var c = asyncMap.computeIfAbsent(redisCodec, this::createAsync);
        return (RedisAsyncCommands<K, V>) c;
    }


    public <K, V> RedisCommands<K, V> sync(RedisCodec<K, V> redisCodec) {
        return (RedisCommands<K, V>) syncMap.computeIfAbsent(redisCodec,
                codec -> RedisUtil.syncHandler(defaultConn(),
                        async(codec), RedisCommands.class));
    }

    public RedisAsyncCommands<String, String> async() {
        return async(DEFAULT_CODES);
    }

    public RedisReactiveCommands<String, String> reactive() {
        return reactive(DEFAULT_CODES);
    }

    public <K, V> RedisReactiveCommands<K, V> reactive(RedisCodec<K, V> redisCodec) {
        var c = reactiveMap.computeIfAbsent(redisCodec, this::createReactive);
        return (RedisReactiveCommands<K, V>) c;
    }

    private <K, V> RedisReactiveCommands<K, V> createReactive(RedisCodec<K, V> codec) {
        var retry = retryTimes > 0 ?
                Retry.fixedDelay(retryTimes, retryDelay).filter(RetryFilter) :
                null;
        return new PooledReactiveCommands<>(pool(), defaultConn(), codec,
                retry);
    }

    private <K, V> StatefulRedisConnection<K, V> defaultConn() {
        return (StatefulRedisConnection<K, V>) defConn;
    }
}
