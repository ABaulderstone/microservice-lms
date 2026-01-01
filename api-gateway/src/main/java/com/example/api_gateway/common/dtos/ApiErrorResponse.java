package com.example.api_gateway.common.dtos;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(Instant timestamp, int status, String error, String message, String path,
        Map<String, List<String>> errors) {

    public static ApiErrorResponse of(HttpStatus httpStatus, String message, String path,
            Map<String, List<String>> errors) {
        return new ApiErrorResponse(Instant.now(), httpStatus.value(), httpStatus.getReasonPhrase(), message, path,
                errors);
    }

    public static ApiErrorResponse of(HttpStatus httpStatus, String message, String path) {
        return new ApiErrorResponse(Instant.now(), httpStatus.value(), httpStatus.getReasonPhrase(), message, path,
                null);
    }
}
