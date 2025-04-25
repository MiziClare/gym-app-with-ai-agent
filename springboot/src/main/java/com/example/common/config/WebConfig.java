package com.example.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements  WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    // Add a custom interceptor JwtInterceptor, set the interceptor rules
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/**")
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/register",
                        "/files/**",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v2/api-docs",
                        "/webjars/**",
                        "/chat/**",
                        "/sendMail",
                        "/front/chat/**",
                        "/front/coachChat/**",
                        "/chat",
                        "/front/chat",
                        "/index.html",
                        "/chatServer/**",
                        "/front/**",
                        "/error/**",
                        "/error"
                );
    }

    // Add a resource handler to ensure that Swagger UI resources can be accessed correctly
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

//     // Add a view controller to handle the front-end routing refresh problem
//     @Override
//     public void addViewControllers(ViewControllerRegistry registry) {
//         registry.addViewController("/{spring:^(?!chatServer|swagger-ui|swagger-resources|v2).*}")
//                 .setViewName("forward:/index.html");
//         registry.addViewController("/**/{spring:^(?!chatServer|swagger-ui|swagger-resources|v2).*}")
//                 .setViewName("forward:/index.html");
//     }

}