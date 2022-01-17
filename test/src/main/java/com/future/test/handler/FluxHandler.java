package com.future.test.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class FluxHandler {
    public Mono<ServerResponse> flux(ServerRequest serverRequest) {
        return ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just("flux test"), String.class);
    }
    public Mono<ServerResponse> flux1(ServerRequest serverRequest) {
        return ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just("flux test1"), String.class);
    }
    public Mono<ServerResponse> serverSendE(ServerRequest serverRequest) {
        return ok().contentType(MediaType.TEXT_EVENT_STREAM).body(
                Flux.interval(Duration.ofSeconds(1))
                .map(t -> Instant.now().toString())
        , String.class);
    }
}
