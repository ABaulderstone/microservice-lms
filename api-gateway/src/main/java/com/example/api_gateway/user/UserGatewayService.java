package com.example.api_gateway.user;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.api_gateway.user.dtos.CreateUserDto;
import com.example.user.proto.v1.CreateUserRequest;
import com.example.user.proto.v1.UserRequest;
import com.example.user.proto.v1.UserResponse;
import com.example.user.proto.v1.UserServiceGrpc.UserServiceBlockingStub;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Service
public class UserGatewayService {
    @GrpcClient("auth-service")
    private UserServiceBlockingStub userServiceStub;

    public Optional<UserResponse> findById(Long id) {
        try {
            var request = UserRequest.newBuilder()
                    .setUserId(id)
                    .build();
            var response = userServiceStub.findUser(request);
            return Optional.of(response);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during findById", e);
        }
    }

    public UserResponse createUser(CreateUserDto data) {
        var request = CreateUserRequest.newBuilder()
                .setEmail(data.email())
                .addAllRoles(data.roles())
                .build();
        return userServiceStub.createUser(request);
    }

}
