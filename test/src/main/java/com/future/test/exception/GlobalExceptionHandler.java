package com.future.test.exception;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/10 22:26
 */
@Configuration
@Order(-2)
public class GlobalExceptionHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        var dbf = serverWebExchange.getResponse().bufferFactory();
        if (throwable instanceof RuntimeException) {
            DataBuffer df = null;
            try {
                df = dbf.wrap(throwable.getMessage().getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
            serverWebExchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return serverWebExchange.getResponse().writeWith(Mono.just(df));
        }
        return Mono.just(null);
    }
}
