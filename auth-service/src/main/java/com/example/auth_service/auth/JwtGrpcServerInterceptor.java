package com.example.auth_service.auth;

import org.springframework.stereotype.Component;

import com.example.auth_service.auth.grpc.GrpcContextKeys;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.jsonwebtoken.Claims;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@Component
@GrpcGlobalServerInterceptor
public class JwtGrpcServerInterceptor implements ServerInterceptor {

    private final JwtService jwtService;

    public JwtGrpcServerInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // skip JWT validation for login method
        if (call.getMethodDescriptor()
                .getFullMethodName()
                .endsWith("/Login")) {
            return next.startCall(call, headers);
        }

        if (call.getMethodDescriptor().getFullMethodName().startsWith("grpc.reflection.")) {
            return next.startCall(call, headers);
        }

        String authHeader = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.close(io.grpc.Status.UNAUTHENTICATED.withDescription("Missing or invalid authorization header"),
                    headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtService.parseToken(token);
            Context ctx = Context.current().withValue(GrpcContextKeys.USER_ID, claims.get("userId", String.class));
            return Contexts.interceptCall(ctx, call, headers, next);
        } catch (Exception e) {
            call.close(io.grpc.Status.UNAUTHENTICATED.withDescription("Invalid token").withCause(e), headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }
    }

}
