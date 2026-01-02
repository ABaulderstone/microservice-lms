package com.example.auth_service.user.grpc;

import com.example.auth_service.user.UserService;
import com.example.auth_service.user.entities.User;
import com.example.user.proto.v1.UserRequest;
import com.example.user.proto.v1.UserResponse;
import com.example.user.proto.v1.UserServiceGrpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    private final UserService userService;

    public UserGrpcService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void findUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        long userId = request.getUserId();
        User foundUser = userService.findById(userId).orElse(null);

        if (foundUser == null) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("User with ID " + userId + " not found")
                    .asRuntimeException());
            return;
        }

        UserResponse response = UserResponse.newBuilder()
                .setId(foundUser.getId())
                .setEmail(foundUser.getEmail())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
