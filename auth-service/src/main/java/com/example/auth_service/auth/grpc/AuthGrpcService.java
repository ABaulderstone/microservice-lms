package com.example.auth_service.auth.grpc;

import com.example.auth.proto.v1.AuthServiceGrpc;
import com.example.auth.proto.v1.LoginRequest;
import com.example.auth.proto.v1.LoginResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private static final String VALID_USERNAME = "student";
    private static final String VALID_PASSWORD = "password123";

    @Override
    public void login(
            LoginRequest request,
            StreamObserver<LoginResponse> responseObserver) {

        boolean success = VALID_USERNAME.equals(request.getUsername()) &&
                VALID_PASSWORD.equals(request.getPassword());

        LoginResponse response;

        if (success) {
            response = LoginResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Login successful")
                    .build();
        } else {
            response = LoginResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid username or password")
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
