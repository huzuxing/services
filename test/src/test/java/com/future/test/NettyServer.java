package com.future.test;

import java.util.*;
import java.util.function.*;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/9 11:40
 */
public class NettyServer {

    private static Supplier<? extends List> dataFun = supplierFun();

    private static Consumer<String> consumer = consumerFun();
    private static BiConsumer<String, Integer> biConsumer = biConsumerFun();
    private static Function<String, Integer> function = transFun();
    private static BiFunction<String, String, Boolean> biFunction = biFunction();
    private static Predicate<? extends Object> predicateFun = predicateFun();


    private static <T extends List> Supplier supplierFun() {
        return () -> Arrays.asList("supplier");
    }

    private static Consumer<String> consumerFun() {
        return v -> System.out.println(Integer.parseInt(v));
    }

    private static BiConsumer<String, Integer> biConsumerFun() {
        return (v1, v2) -> {
            if ("bi".equals(v1)) {
                v2 = v2 + 32;
                System.out.println(v2);
            }
        };
    }

    private static Function<String, Integer> transFun() {
        return v -> {
            if ("fun".equals(v)) {
                return 1;
            }
            return -1;
        };
    }

    private static BiFunction<String, String, Boolean> biFunction() {
        return (v1, v2) -> {
            return v1.equals(v2);
        };
    }
    private static Predicate<? extends Object> predicateFun() {
        return v -> Objects.nonNull(v);
    }

    public static void main(String[] args) {
       // ServerBootstrap b = new ServerBootstrap();
        var s = new HashMap<Integer, Integer>();
        s.put(1, 1);
        s.put(1, 11);
        System.out.println(s.get(1));
        System.out.println(s.put(1, 22));
        List<String> names = Arrays.asList("Peter", "Jim", "Themis");
        Collections.sort(names, (a, b) -> a.compareTo(b));
        System.out.println(names);
        System.out.println(dataFun.get());
        consumer.accept("3");
        biConsumer.accept("bi", 32);
        System.out.println(function.apply("fun"));
        System.out.println(biFunction.apply("1", "1"));
        System.out.println(predicateFun.test(null));
        IntFunction<Date[]> intFunction = Date[]::new;
        Date[] dates = intFunction.apply(10);
    }

    public static void test(Consumer<String> consumer) {
        consumer.accept("32");
    }

    public static Supplier<? extends List> getDataFun() {
        return dataFun;
    }
}

interface FunIn {

}
class f1 implements FunIn {

}
