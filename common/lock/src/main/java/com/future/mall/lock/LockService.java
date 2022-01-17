package com.future.mall.lock;

import java.util.concurrent.locks.Lock;

/**
 * @Author huzuxing
 * @description todo
 **/
public interface LockService {
    boolean lock(String lockKey);
    boolean release(String lockKey);
    Lock newLock(String lockKey);
}
