package com.future.test;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/9/26 17:06
 */
public class Singleton {
    private static volatile Singleton singleton;
    private Singleton() {}
    public static Singleton getInstance() {
        if (null == singleton) {
            synchronized (Singleton.class) {
                if (null == singleton) {
                    singleton = new Singleton();
                }
            }
        }
        return singleton;
    }
}

class Singleton1 {
    private Singleton1() {}
    static class Builder {
        private static final Singleton1 instance = new Singleton1();
        public static Singleton1 build() {
            return instance;
        }
    }

    public static Singleton1 getInstance() {
        return Builder.build();
    }
}
