package com.example.api_gateway.auth;

import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.api_gateway.common.GrpcAuthConstants;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

@GrpcGlobalClientInterceptor
@Component
public class UserContextGrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> respListener, Metadata headers) {
                if (auth != null && auth.isAuthenticated()) {
                    headers.put(GrpcAuthConstants.USER_ID, auth.getPrincipal().toString());
                    headers.put(GrpcAuthConstants.USER_ROLES, auth.getAuthorities().stream()
                            .map(authority -> authority.getAuthority()).collect(Collectors.joining(",")));
                }
                super.start(respListener, headers);
            }
        };
    }

}
