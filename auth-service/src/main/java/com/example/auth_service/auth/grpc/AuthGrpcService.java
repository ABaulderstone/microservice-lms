package com.example.auth_service.auth.grpc;

import com.example.auth.proto.v1.AuthServiceGrpc;
import com.example.auth.proto.v1.LoginRequest;
import com.example.auth.proto.v1.LoginResponse;
import com.example.auth.proto.v1.ValidateTokenRequest;
import com.example.auth.proto.v1.ValidateTokenResponse;
import com.example.auth_service.auth.PasswordEncodingService;
import com.example.auth_service.jwt.JwtService;
import com.example.auth_service.user.UserService;
import com.example.auth_service.user.entities.User;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

        private final UserService userService;
        private final PasswordEncodingService passwordEncodingService;
        private final JwtService jwtService;

        public AuthGrpcService(
                        UserService userService,
                        PasswordEncodingService passwordEncodingService,
                        JwtService jwtService) {
                this.userService = userService;
                this.passwordEncodingService = passwordEncodingService;
                this.jwtService = jwtService;
        }

        @Override
        public void login(
                        LoginRequest request,
                        StreamObserver<LoginResponse> responseObserver) {

                String email = request.getEmail();
                User foundUser = userService.findByEmail(email).orElse(null);

                if (foundUser == null) {
                        responseObserver
                                        .onError(Status.UNAUTHENTICATED.withDescription("Email or password invalid")
                                                        .asRuntimeException());
                        return;
                }

                boolean passwordMatches = passwordEncodingService.verifyPassword(
                                request.getPassword(),
                                foundUser.getPassword());

                if (!passwordMatches) {
                        responseObserver
                                        .onError(Status.UNAUTHENTICATED.withDescription("Email or password invalid")
                                                        .asRuntimeException());

                }

                String token = jwtService.generateToken(foundUser);

                LoginResponse response = LoginResponse.newBuilder()
                                .setToken(token)
                                .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();

        }

        @Override
        public void validateToken(
                        ValidateTokenRequest request,
                        StreamObserver<ValidateTokenResponse> responseObserver) {

                String token = request.getToken();
                boolean isValid = jwtService.validateToken(token);
                ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                                .setValid(isValid)
                                .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }
}