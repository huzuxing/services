package com.future.common.redis.manager;

import reactor.core.publisher.Mono;
import reactor.pool.InstrumentedPool;
import reactor.pool.PooledRef;
import reactor.pool.PooledRefMetadata;

import java.time.Duration;
import java.util.Objects;

class SingletonPool<POOLABLE> implements InstrumentedPool<POOLABLE>, InstrumentedPool.PoolMetrics {

    final Mono<PooledRef<POOLABLE>> publisher;

    SingletonPool(POOLABLE poolable) {
        Objects.requireNonNull(poolable);
        this.publisher = Mono.just(new SingletonPooledRef<>(poolable));
    }

    @Override
    public PoolMetrics metrics() {
        return this;
    }

    @Override
    public int acquiredSize() {
        return 1;
    }

    @Override
    public int allocatedSize() {
        return 1;
    }

    @Override
    public int idleSize() {
        return 1;
    }

    @Override
    public int pendingAcquireSize() {
        return 1;
    }

    @Override
    public int getMaxAllocatedSize() {
        return 1;
    }

    @Override
    public int getMaxPendingAcquireSize() {
        return 1;
    }

    @Override
    public Mono<Integer> warmup() {
        return Mono.empty();
    }

    @Override
    public Mono<PooledRef<POOLABLE>> acquire() {
        return publisher;
    }

    @Override
    public Mono<PooledRef<POOLABLE>> acquire(Duration timeout) {
        return publisher;
    }

    @Override
    public Mono<Void> disposeLater() {
        return Mono.empty();
    }


    static class SingletonPooledRef<T> implements PooledRef<T>, PooledRefMetadata {

        final T poolable;

        SingletonPooledRef(T poolable) {
            this.poolable = poolable;
        }

        @Override
        public T poolable() {
            return poolable;
        }

        @Override
        public PooledRefMetadata metadata() {
            return this;
        }

        @Override
        public Mono<Void> invalidate() {
            return null;
        }

        @Override
        public Mono<Void> release() {
            return Mono.empty();
        }

        @Override
        public int acquireCount() {
            return 1;
        }

        @Override
        public long idleTime() {
            return 0;
        }

        @Override
        public long lifeTime() {
            return 0;
        }

        @Override
        public long releaseTimestamp() {
            return 0;
        }

        @Override
        public long allocationTimestamp() {
            return 0;
        }
    }

}
