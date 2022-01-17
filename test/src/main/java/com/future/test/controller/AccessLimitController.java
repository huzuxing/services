package com.future.test.controller;

import com.future.common.annotation.AccessLimit;
import com.future.exception.constant.enums.CommonResponseEnum;
import com.future.exception.i18n.UnifiedMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.WebUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;

@RestController
public class AccessLimitController {

    @Resource
    private UnifiedMessageSource unifiedMessageSource;

    @AccessLimit(seconds = 10, maxCount = 5, needLogin = true)
    @RequestMapping("/access")
    @ResponseBody
    public Mono<String> accessLimit(HttpServletRequest request, String name) {
        var s = unifiedMessageSource.getMessage("response.success");
        CommonResponseEnum.SERVER_BUSY.assertNotEmpty(name);
        System.out.println(request.getContentType());
        System.out.println(request.getParameter("name"));
        System.out.println(request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE));
        System.out.println("requestURI:" + request.getRequestURI());
        Enumeration<?> enums = request.getAttributeNames();
        while (enums.hasMoreElements()) {
            String v = (String) enums.nextElement();
            System.out.println(v);
        }
        var method = request.getMethod();
        if (HttpMethod.GET.matches(method)) {
            System.out.println("get request");
        }

        return Mono.just(s + "," + name);
    }

    @RequestMapping(value = "flux3", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Object> flux(ServerResponse serverResponse) {
        serverResponse.headers();
        return Flux.interval(Duration.ofSeconds(1))
                .map(t -> Instant.now().toString());
    }
}
