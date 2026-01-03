package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.api_gateway.auth.JwtGrpcClientInterceptor;

import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;

@Configuration
public class GrpcClientConfig {
    @Bean
    public GlobalClientInterceptorConfigurer jwtGrpcClientInterceptorConfigurer(
            JwtGrpcClientInterceptor jwtGrpcClientInterceptor) {
        return registry -> registry.add(jwtGrpcClientInterceptor);
    }
}
