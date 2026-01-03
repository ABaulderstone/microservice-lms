package com.example.auth_service.auth.grpc;

import io.grpc.Context;

public final class GrpcContextKeys {
    private GrpcContextKeys() {
    }

    public static final Context.Key<String> USER_ID = Context.key("userId");
}
