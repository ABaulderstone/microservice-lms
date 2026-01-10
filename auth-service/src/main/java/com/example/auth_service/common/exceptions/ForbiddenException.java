package com.example.auth_service.common.exceptions;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class ForbiddenException extends StatusRuntimeException {
    public ForbiddenException(String message) {
        super(Status.PERMISSION_DENIED.withDescription(message));
    }

}