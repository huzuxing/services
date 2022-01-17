package com.future.test.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Configuration
@Slf4j
public class AccessWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
       var m = serverWebExchange.getRequest().getPath().toString();
        log.info("{}, begin time: {}",m, Instant.now());
        return webFilterChain.filter(serverWebExchange).doFinally(r -> {
            log.info("res time: {}", Instant.now());
        });
    }
}
