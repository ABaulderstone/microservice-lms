package com.example.api_gateway.auth;

import io.grpc.Metadata;

public class GrpcAuthConstants {
    public static final String AUTH_SERVICE_NAME = "auth-service";
    public static final Metadata.Key<String> AUTH_TOKEN_METADATA_KEY = Metadata.Key.of("authorization",
            Metadata.ASCII_STRING_MARSHALLER);
}
