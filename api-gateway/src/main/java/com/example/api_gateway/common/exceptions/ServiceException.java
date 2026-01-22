package com.example.api_gateway.common.exceptions;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class ServiceException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final Map<String, List<String>> errors;

    public ServiceException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errors = null;
    }

    public ServiceException(StatusRuntimeException grpcException) {
        super(grpcException.getStatus().getDescription() != null
                ? grpcException.getStatus().getDescription()
                : "Service error");
        this.httpStatus = mapGrpcStatusToHttp(grpcException.getStatus().getCode());
        this.errors = null;
    }

    public ServiceException(HttpStatus httpStatus, String message, Map<String, List<String>> errors) {
        super(message);
        this.httpStatus = httpStatus;
        this.errors = errors;
    }

    public ServiceException(StatusRuntimeException grpcException, String customMessage) {
        super(customMessage);
        this.httpStatus = mapGrpcStatusToHttp(grpcException.getStatus().getCode());
        this.errors = null;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    private static HttpStatus mapGrpcStatusToHttp(Status.Code grpcStatus) {
        return switch (grpcStatus) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case FAILED_PRECONDITION -> HttpStatus.BAD_REQUEST;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}