package com.future.mall.lock;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @Author huzuxing
 * @description todo
 **/
public class KeyLock implements Lock {
    private final String key;
    private final LockService lockService;

    public KeyLock(String key, LockService lockService) {
        this.key = Objects.requireNonNull(key);
        this.lockService = Objects.requireNonNull(lockService);
    }

    @Override
    public void lock() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryLock() {
        return lockService.lock(key);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlock() {
        lockService.release(key);
    }

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException();
    }
}
