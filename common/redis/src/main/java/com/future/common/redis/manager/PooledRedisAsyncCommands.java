package com.future.common.redis.manager;

import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.RedisCommand;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import reactor.pool.InstrumentedPool;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;
import java.util.function.Supplier;


@Slf4j
public class PooledRedisAsyncCommands<K, V> extends RedisAsyncCommandsImpl<K, V>
        implements RedisAsyncCommands<K, V> {


    private final InstrumentedPool<StatefulRedisConnection<K, V>> pool;

    private final EventExecutorGroup scheduler;
    private final int retryTimes;
    private final Duration retryDelay;
    private final Predicate<Throwable> retryFilter;

    /**
     * Initialize a new connection.
     *
     * @param connection the connection .
     * @param codec      Codec used to encode/decode keys and values.
     * @param retryTimes
     * @param retryDelay
     */
    public PooledRedisAsyncCommands(
            InstrumentedPool<StatefulRedisConnection<K, V>> pool,
            StatefulRedisConnection<K, V> connection,
            RedisCodec<K, V> codec,
            int retryTimes, Duration retryDelay,
            Predicate<Throwable> retryFilter) {
        super(connection, codec);
        this.pool = pool;
        this.retryTimes = retryTimes;
        this.retryDelay = retryDelay;
        this.retryFilter = retryFilter;

        EventExecutorGroup scheduler = ImmediateEventExecutor.INSTANCE;
        if (connection.getOptions().isPublishOnScheduler()) {
            scheduler = connection.getResources().eventExecutorGroup();
        }
        this.scheduler = scheduler;
    }

    /***
     * rewrite for pool
     * @param connection
     * @param cmd
     * @param <T>
     * @return
     */

    <T> AsyncCommand<K, V, T> doDispatch(
            StatefulRedisConnection<K, V> connection,
            RedisCommand<K, V, T> cmd) {
        var asyncCommand = new AsyncCommand<>(cmd);
        var dispatched = connection.dispatch(asyncCommand);
        if (dispatched instanceof AsyncCommand) {
            return (AsyncCommand<K, V, T>) dispatched;
        }
        return asyncCommand;
    }

    <T> void dispatchInPool(RedisCommand<K, V, T> cmd, CompletableFuture<T> result) {
        pool.acquire().doOnCancel(() -> {
            result.completeExceptionally(new CancellationException());
        }).doOnError(result::completeExceptionally).doOnSuccess(ref -> {
            if (null == ref) {
                result.completeExceptionally(
                        new IllegalStateException("borrow null ref"));
                return;
            }
            try {
                var connection = ref.poolable();
                var re = doDispatch(connection, cmd);
                re.whenCompleteAsync((t, e) -> {
                    ref.release().subscribe();
                    if (null == e) {
                        result.complete(t);
                    } else {
                        result.completeExceptionally(e);
                    }
                }, scheduler.next());
            } catch (Throwable e) {
                ref.release().subscribe();
                result.completeExceptionally(e);
            }
        }).subscribe();
    }

    public static <T, F extends CompletableFuture<T>> void transform(CompletionStage<T> stage, F f) {
        stage.whenComplete((t, e) -> {
            if (null == e) f.complete(t);
            else f.completeExceptionally(e);
        });
    }

    /**
     * @param source     start future source
     * @param retryTimes retry times
     * @param retryDelay retry delay time, 0-retry immediately
     */
    public static <T> RetryFuture<T> retry(
            Supplier<CompletableFuture<T>> source,
            int retryTimes, Duration retryDelay,
            Predicate<? super Throwable> errorFilter) {
        var task = new RetryFuture<>(source, retryTimes, retryDelay,
                errorFilter);
        task.run();
        return task;
    }

    @Override
    public <T> AsyncCommand<K, V, T> dispatch(RedisCommand<K, V, T> cmd) {
        var result = new AsyncCommand<>(cmd);
        if (retryTimes > 0) {
            transform(retry(() -> {
                var f = new CompletableFuture<T>();
                dispatchInPool(cmd, f);
                return f;
            }, retryTimes, retryDelay, retryFilter), result);
        } else {
            dispatchInPool(cmd, result);
        }
        return result;
    }

    /**
     * forbidden methods
     * @return
     */

    @Override
    public StatefulRedisConnection<K, V> getStatefulConnection() {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public StatefulConnection<K, V> getConnection() {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public RedisFuture<String> multi() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RedisFuture<String> auth(CharSequence password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RedisFuture<String> select(int db) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RedisFuture<String> swapdb(int db1, int db2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushCommands() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown(boolean save) {
        throw new UnsupportedOperationException();
    }


}
