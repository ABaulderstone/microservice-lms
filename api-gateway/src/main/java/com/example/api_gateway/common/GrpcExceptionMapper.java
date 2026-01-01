package com.example.api_gateway.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import io.grpc.StatusRuntimeException;

public class GrpcExceptionMapper {

    public static RuntimeException toHttpException(StatusRuntimeException e) {
        return switch (e.getStatus().getCode()) {
            case UNAUTHENTICATED -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    e.getStatus().getDescription());
            case PERMISSION_DENIED -> new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    e.getStatus().getDescription());
            case NOT_FOUND -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getStatus().getDescription());
            default -> new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Downstream service error",
                    e);
        };
    }
}
