package com.example.auth_service.common;

import java.util.Set;

import org.springframework.stereotype.Component;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@GrpcGlobalServerInterceptor
@Component
public class UserContextServerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String userId = headers.get(GrpcAuthConstants.USER_ID);
        String userRoles = headers.get(GrpcAuthConstants.USER_ROLES);

        RequestContext.setUserContext(new UserContext(userId,
                userRoles != null ? Set.of(userRoles.split(",")) : Set.of()));

        // Clear the context after the call is complete
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                next.startCall(call, headers)) {
            @Override
            public void onComplete() {
                RequestContext.clear();
                super.onComplete();
            }

            @Override
            public void onCancel() {
                RequestContext.clear();
                super.onCancel();
            }
        };

    }
}
