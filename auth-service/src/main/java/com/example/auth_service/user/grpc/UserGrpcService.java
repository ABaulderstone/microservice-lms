package com.example.auth_service.user.grpc;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.auth_service.common.decorators.RequireAnyRole;
import com.example.auth_service.common.exceptions.RolesNotFoundException;
import com.example.auth_service.common.exceptions.UserAlreadyExistsException;
import com.example.auth_service.user.UserService;
import com.example.auth_service.user.entities.Role;
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
        @RequireAnyRole({ "TALENT", "ADMIN" })
        public void findUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
                long userId = request.getUserId();
                User foundUser = userService.findById(userId).orElse(null);

                if (foundUser == null) {
                        responseObserver.onError(
                                        Status.NOT_FOUND.withDescription("User with ID " + userId + " not found")
                                                        .asRuntimeException());
                        return;
                }

                UserResponse response = UserResponse.newBuilder()
                                .setId(foundUser.getId())
                                .setEmail(foundUser.getEmail())
                                .addAllRoles(foundUser.getRoles().stream()
                                                .map(role -> role.getName().name())
                                                .toList())
                                .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
        }

        @Override
        @RequireAnyRole({ "ADMIN" })
        public void createUser(com.example.user.proto.v1.CreateUserRequest request,
                        StreamObserver<UserResponse> responseObserver) {
                try {
                        String email = request.getEmail();
                        String password = "password123"; // Default password, should be changed later
                        var roles = request.getRolesList(); // Assuming roles are sent as a list of strings
                        Set<Role.RoleName> roleNames = roles.stream()
                                        .map(Role.RoleName::valueOf)
                                        .collect(Collectors.toSet());

                        User newUser = userService.createUser(email, password, roleNames);

                        UserResponse response = UserResponse.newBuilder()
                                        .setId(newUser.getId())
                                        .setEmail(newUser.getEmail())
                                        .addAllRoles(newUser.getRoles().stream()
                                                        .map(role -> role.getName().name())
                                                        .toList())
                                        .build();

                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                } catch (UserAlreadyExistsException e) {
                        responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage())
                                        .asRuntimeException());
                } catch (RolesNotFoundException e) {
                        responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage())
                                        .asRuntimeException());
                } catch (IllegalArgumentException e) {
                        responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage())
                                        .asRuntimeException());
                } catch (Exception e) {
                        responseObserver.onError(Status.INTERNAL.withDescription("Internal server error")
                                        .asRuntimeException());
                }
        }
}
