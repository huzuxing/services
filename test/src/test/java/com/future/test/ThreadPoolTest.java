package com.future.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/9/26 14:35
 */
public class ThreadPoolTest {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        ExecutorService pool1 = Executors.newSingleThreadExecutor();
        ScheduledExecutorService pool2 = Executors.newScheduledThreadPool(3);
        ScheduledExecutorService pool3 = Executors.newSingleThreadScheduledExecutor();
//        for (int i = 0; i < 5; i++) {
//            pool1.execute(() -> {
//                System.out.println(Thread.currentThread().getName() + ", runing");
//            });
//        }
//        pool1.shutdown();

//        pool2.scheduleAtFixedRate(() -> {
//            System.out.println(Thread.currentThread().getName() + ", schedule task");
//        }, 1000, 1000, TimeUnit.MILLISECONDS);

        pool3.schedule(() -> {
            System.out.println(Thread.currentThread().getName() + ", single task");
        }, 1000, TimeUnit.MILLISECONDS);
        pool3.scheduleWithFixedDelay(() -> {
            System.out.println(Thread.currentThread().getName() + ", single task scheduleWithFixedDelay");
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }
}
