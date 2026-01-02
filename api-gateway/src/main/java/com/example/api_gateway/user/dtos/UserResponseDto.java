package com.example.api_gateway.user.dtos;

import com.example.user.proto.v1.UserResponse;

public record UserResponseDto(Long id, String email) {
    public static UserResponseDto fromProto(UserResponse proto) {
        return new UserResponseDto(proto.getId(), proto.getEmail());
    }
}
