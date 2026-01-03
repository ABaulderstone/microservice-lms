package com.example.api_gateway.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

@Component
public class JwtGrpcClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions, Channel next) {

        // No JWT needed to be passed to AuthService for login
        if (method.getFullMethodName().endsWith("/Login")) {
            return next.newCall(method, callOptions);
        }
        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getCredentials() != null) {
                    String token = RequestContextHolder.currentRequestAttributes()
                            .getAttribute("jwt", 0).toString();
                    headers.put(GrpcAuthConstants.AUTH_TOKEN_METADATA_KEY, "Bearer " + token);
                }
                super.start(responseListener, headers);
            }
        };
    }

}
