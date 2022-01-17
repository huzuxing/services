package com.future.mall.lock;

import java.util.concurrent.locks.Lock;

/**
 * @Author huzuxing
 * @description todo
 **/
public abstract class AbstractLockService implements LockService{
    @Override
    public Lock newLock(String lockKey) {
        return new KeyLock(lockKey, this);
    }
}
