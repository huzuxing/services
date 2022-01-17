package com.future.locktest;

import com.future.mall.lock.LockService;
import com.future.test.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wildfly.common.Assert;

/**
 * @Author huzuxing
 * @description todo
 **/
@SpringBootTest(classes = Application.class)
public class LockTest {

    @Autowired
    private LockService redisLockService;

    @Test
    public void lockTest() throws InterruptedException {
        var lock = redisLockService.lock("testKey");
        Assert.assertTrue(lock);
        Thread.sleep(2000);
        var res = redisLockService.release("testKey");
        Assert.assertTrue(res);
    }

    @Test
    public void unlock() {
        var res = redisLockService.release("testKey");
        Assert.assertTrue(res);
    }
}
