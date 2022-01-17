package com.future.test;

import java.util.concurrent.Callable;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/9/26 13:57
 */
public class ThreadTest {
    private static final Object s1 = new Object();
    private static final Object s2 = new Object();
    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (s1) {
                System.out.println(Thread.currentThread().getName() + " get s1 lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " waiting get s2 lock");
                synchronized (s2) {
                    System.out.println(Thread.currentThread().getName() + " get s2 lock");
                }
            }
        }, "Thread-1").start();

        new Thread(() -> {
            synchronized (s1) {
                System.out.println(Thread.currentThread().getName() + " get s2 lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " waiting get s1 lock");
                synchronized (s2) {
                    System.out.println(Thread.currentThread().getName() + " get s1 lock");
                }
            }
        }, "Thread-2").start();
    }
}

class myCallable implements Callable<String> {

    @Override
    public String call() throws Exception {
        return null;
    }
}
