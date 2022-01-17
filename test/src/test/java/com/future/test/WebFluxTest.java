package com.future.test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class WebFluxTest {
    public static void main(String[] args) {
        var m = Mono.just("test");
        m.subscribe(System.out::println);
        var p = new Person();
        var p1 = new Person();
        System.out.println(p.equals(p1));
        Flux.fromIterable(Arrays.asList(1, 2, 3).subList(0 ,2)).map(ls -> {
            var f = CompletableFuture.supplyAsync(() -> {
                return "Q" +ls;
            });
            return f;
        }).subscribe(f -> {
            f.whenComplete((v, e) -> {
                System.out.println(v);
            });
        });
        System.out.println(">>>>>>>>>>>>>>>>");
        Flux.create(sink -> {
            for (int i = 0; i < 1; i++) {
                sink.next(i);
            }
            sink.complete();
        }).subscribe(System.out::println);

        Flux.just("a", "b").zipWith(Flux.just("c", "d")).subscribe(System.out::println);
        Flux.just("a", "b").zipWith(Flux.just("c", "d"), (v1, v2) -> String.format("%s-%s", v1, v2)).subscribe(System.out::println);
    }
}


class Person {

    private String idCard;


}
