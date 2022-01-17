package com.future.common.redis.manager;

import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.protocol.RedisCommand;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.pool.InstrumentedPool;
import reactor.util.retry.Retry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.future.common.redis.manager.RedisUtil.unSupportMethod;


@Slf4j
@SuppressWarnings("unchecked")
class PooledReactiveCommands<K, V> extends RedisReactiveCommandsImpl<K, V> implements RedisReactiveCommands<K, V> {

    private static final Constructor<Publisher<?>> constructor;

    static {
        try {
            var clz = Class.forName("io.lettuce.core.RedisPublisher");
            constructor = (Constructor<Publisher<?>>)
                    clz.getConstructor(Supplier.class, StatefulConnection.class,
                            boolean.class, Executor.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final InstrumentedPool<StatefulRedisConnection<K, V>> pool;

    private final EventExecutorGroup scheduler;
    private final Retry retry;

    /**
     * Initialize a new instance.
     *
     * @param connection the connection to operate on.
     * @param codec      the codec for command encoding.
     * @param retry
     */
    PooledReactiveCommands(
            InstrumentedPool<StatefulRedisConnection<K, V>> pool,
            StatefulRedisConnection<K, V> connection,
            RedisCodec<K, V> codec, Retry retry) {
        super(connection, codec);
        this.pool = pool;
        this.retry = retry;

        EventExecutorGroup scheduler = ImmediateEventExecutor.INSTANCE;
        if (connection.getOptions().isPublishOnScheduler()) {
            scheduler = connection.getResources().eventExecutorGroup();
        }
        this.scheduler = scheduler;
    }

    /**
     * rewrite for pool
     * @param connection
     * @param commandSupplier
     * @param dissolve
     * @param <T>
     * @return
     */
    private <T> Publisher<T> newPublisher(
            StatefulRedisConnection<K, V> connection,
            Supplier<RedisCommand<K, V, T>> commandSupplier,
            boolean dissolve) {
        try {
            return (Publisher<T>) constructor.newInstance(
                    commandSupplier, connection, dissolve,
                    scheduler.next());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        }
    }


    @Override
    public <T, R> Flux<R> createDissolvingFlux(Supplier<RedisCommand<K, V, T>> commandSupplier) {
        return (Flux<R>) createFlux(commandSupplier, true);
    }

    @Override
    public <T> Flux<T> createFlux(Supplier<RedisCommand<K, V, T>> commandSupplier) {
        return createFlux(commandSupplier, false);
    }

    private <T> Flux<T> createFlux(Supplier<RedisCommand<K, V, T>> commandSupplier, boolean dissolve) {
        var flux = Flux.usingWhen(pool.acquire(), ref -> {
            var publisher = newPublisher(ref.poolable(), commandSupplier, dissolve);
            return Flux.from(publisher);
        }, RedisUtil.poolRelease());
        return null == retry ? flux : flux.retryWhen(retry);
    }

    @Override
    public <T> Mono<T> createMono(Supplier<RedisCommand<K, V, T>> commandSupplier) {
        var mono = Mono.usingWhen(pool.acquire(), ref -> {
            var publisher = newPublisher(ref.poolable(), commandSupplier, false);
            return Mono.from(publisher);
        }, RedisUtil.poolRelease());
        return null == retry ? mono : mono.retryWhen(retry);
    }

    /**
     * forbidden methods
     * @return
     */

    @Override
    public StatefulRedisConnection<K, V> getStatefulConnection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StatefulConnection<K, V> getConnection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<String> auth(CharSequence password) {
        return unSupportMethod();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<String> multi() {
        throw new UnsupportedOperationException();
    }


    @Override
    public Mono<String> select(int db) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<String> swapdb(int db1, int db2) {
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
    public void setTimeout(Duration timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> shutdown(boolean save) {
        throw new UnsupportedOperationException();
    }


}
