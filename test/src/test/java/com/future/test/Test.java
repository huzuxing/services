package com.future.test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/9/23 17:09
 */
public class Test {
    public static void main(String[] args) {
        var strs = new String[]{"hello", "world"};
        Arrays.stream(strs).map(v -> v.split("")).flatMap(Arrays::stream).forEach(System.out::println);
        Arrays.stream(strs).flatMap(v -> Arrays.stream(v.split(""))).forEach(System.out::println);
        System.out.println(2 << 3);
        short s1 = 1;
        s1 = (short) (s1 + 1);
        s1 += 1;
        System.gc();

        loop:
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    System.out.println(k);
                    if (k == 5) {
                        System.out.println("k == 5, break");
                        break loop;
                    }
                }
            }
        }
        var m = new Man();
        char c = 'f';
        System.out.println(c + 3);
        String s = "";
//        var ll = new CopyOnWriteArrayList<>();
//        ll.addAll(List.of(1,2,3,4,5,6,7));
        var ll = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ll.add(i + 1);
        }
        Iterator it = ll.iterator();
        while (it.hasNext()) {
            var v = (Integer)it.next();
            System.out.println(v);
            if (v == 5) {
                it.remove();
                break;
            }
        }
        System.out.println(ll);
        var aa = ll.toArray(new Integer[]{});
        System.out.println(aa);
        var aa1= Arrays.copyOf(aa, 4);
        for (Integer a1 : aa1) {
            System.out.println("===, " + a1);
        }

        var syncList = Collections.synchronizedList(ll);
        for (int i = 0; i < 20; i++) {
            final int r = i;
            new Thread(() -> {
                syncList.add(r);
            }).start();
        }
        System.out.println(syncList);

        var ss = new HashSet<>();
        int size = aa.length - 1;
        System.out.println("size," + size);
        System.out.println(size & 64234352);
        var chm = new ConcurrentHashMap<>();
        var hs = new ArrayList<Man>();
        for (int i = 0; i < 10; i++) {
            var man = new Man();
            man.setName("" + i);
            hs.add(man);
        }
        hs.sort(Comparator.comparing(Man::getName));
        System.out.println(hs);
    }

}

class Human {

    private String id;

    public Human() {
    }

    public Human(String id) {
        this.id = id;
    }
}

class Man extends Human {
    private String name;
    public Man() {
        super();
    }

    public Man(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Man{" +
                "name='" + name + '\'' +
                '}';
    }
}
