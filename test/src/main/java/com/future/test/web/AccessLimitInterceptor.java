//package com.future.test.web;
//
//import com.future.common.annotation.AccessLimit;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import java.util.Optional;
//
//@Component
//@Slf4j
//public class AccessLimitInterceptor extends HandlerInterceptorAdapter {
//    @Override
//    public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler) throws Exception {
//        HandlerMethod hm = (HandlerMethod) handler;
//        Optional<AccessLimit> limit = Optional.ofNullable(hm.getMethodAnnotation(AccessLimit.class));
//        if (!limit.isPresent()) return true;
//
//        int maxCount = limit.get().maxCount();
//        long seconds = limit.get().seconds();
//        boolean needLogin = limit.get().needLogin();
//        log.info("maxCount: {}, seconds: {}, needLogin: {}", maxCount, seconds, needLogin);
//        return true;
//    }
//
//    @Override
//    public void postHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        super.postHandle(request, response, handler, modelAndView);
//    }
//}
