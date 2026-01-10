package com.example.api_gateway.user.dtos;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.user.proto.v1.UserResponse;

public record UserResponseDto(Long id, String email, Set<String> roles) {
    public static UserResponseDto fromProto(UserResponse proto) {
        return new UserResponseDto(proto.getId(), proto.getEmail(),
                proto.getRolesList().stream().collect(Collectors.toSet()));
    }
}
