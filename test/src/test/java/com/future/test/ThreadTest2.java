package com.future.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/9/26 15:49
 */
public class ThreadTest2 {
    private final static Object lock = new Object();
    private static  int count = 0;
    private static final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private static final ThreadLocal tl = new ThreadLocal();
    private static final Lock l = new ReentrantLock();
    public static void main(String[] args) {
        //apartSync();
        for (int i = 0; i < 3; i++) {
            var ct = i;
            new Thread(() -> {
                count++;
            }).start();
        }
        //
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("i: " + count);
        tl.set("threadlocaltest");
        System.out.println(tl.get());
        tl.remove();
        int _1m = 1024 * 1024;
        byte[] b = new byte[_1m];
        b = null;
        System.gc();
        System.out.println(Thread.currentThread().getId());
        int orderId = 232141233;
        int dbIndex = orderId % 32;
        System.out.println("dbIndex: " + dbIndex);
        int tableIndex = orderId / 32 % 32;
        System.out.println("tableIndex: " + tableIndex);
    }

    public synchronized static void syncTest() {

    }

    public static void apartSync() {
        synchronized (lock) {
            System.out.println(3223);
        }
    }

    public static void lock() {
        rwl.readLock().lock();
        rwl.writeLock();
    }

    public static void rlock() {
        l.lock();

        l.unlock();
    }
}
