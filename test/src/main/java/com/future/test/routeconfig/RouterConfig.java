package com.future.test.routeconfig;

import com.future.test.handler.FluxHandler;
import com.future.test.web.AccessWebFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Component
public class RouterConfig {
    @Autowired
    FluxHandler fluxHandler;

    @Autowired
    AccessWebFilter accessWebFilter;

    @Bean
    public RouterFunction<ServerResponse> fluxRoute() {
        return RouterFunctions
                .route(GET("/flux"), fluxHandler::flux)
                .andRoute(GET("/flux1"), fluxHandler::flux1)
                .andRoute(GET("/serverSendE"), fluxHandler::serverSendE);

    }
}
