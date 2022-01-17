//package com.future.test.web;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
//
//@Configuration
//public class WebInterceptorConfiguration extends WebMvcConfigurationSupport {
//
//    @Autowired
//    private AccessLimitInterceptor accessLimitInterceptor;
//
//    @Override
//    protected void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(accessLimitInterceptor)
//                .addPathPatterns("/**");
//        super.addInterceptors(registry);
//    }
//}
