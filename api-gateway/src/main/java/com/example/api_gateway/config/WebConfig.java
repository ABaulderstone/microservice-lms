package com.example.api_gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.api_gateway.auth.RequireRolesInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String API_PREFIX = "/api/v1";
    private final RequireRolesInterceptor requireRolesInterceptor;

    public WebConfig(RequireRolesInterceptor requireRolesInterceptor) {
        this.requireRolesInterceptor = requireRolesInterceptor;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_PREFIX, c -> !c.getPackageName().startsWith("org.springdoc"));

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requireRolesInterceptor)
                .addPathPatterns(API_PREFIX + "/**");
    }
}
