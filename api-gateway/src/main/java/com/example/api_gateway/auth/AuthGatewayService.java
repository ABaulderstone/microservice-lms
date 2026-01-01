package com.example.api_gateway.auth;

import com.example.api_gateway.auth.dtos.LoginRequestDto;
import com.example.auth.proto.v1.AuthServiceGrpc;
import com.example.auth.proto.v1.LoginRequest;
import com.example.auth.proto.v1.LoginResponse;
import java.util.Map;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class AuthGatewayService {

    @GrpcClient("auth-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    public Map<String, String> login(LoginRequestDto request) {
        try {
            LoginRequest grpcRequest = LoginRequest.newBuilder()
                    .setEmail(request.email())
                    .setPassword(request.password())
                    .build();

            LoginResponse grpcResponse = authServiceStub.login(grpcRequest);
            return Map.of("token", grpcResponse.getToken());
        } catch (Exception e) {
            throw new RuntimeException("Failed to login user", e);
        }
    }
}
