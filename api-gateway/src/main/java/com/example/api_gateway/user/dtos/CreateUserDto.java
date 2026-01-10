package com.example.api_gateway.user.dtos;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateUserDto(@NotBlank @Email String email, @NotEmpty Set<String> roles) {

}
