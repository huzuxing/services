package com.future.mall.lock.local;

import com.future.mall.lock.AbstractLockService;
import com.future.mall.lock.LockService;

/**
 * @Author huzuxing
 * @description todo
 **/
public class LocalLockService extends AbstractLockService implements LockService {
    @Override
    public boolean lock(String lockKey) {
        return false;
    }

    @Override
    public boolean release(String lockKey) {
        return false;
    }
}
