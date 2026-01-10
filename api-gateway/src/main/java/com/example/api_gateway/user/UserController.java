package com.example.api_gateway.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.api_gateway.auth.RequireRoles;
import com.example.api_gateway.user.dtos.CreateUserDto;
import com.example.api_gateway.user.dtos.UserResponseDto;
import com.example.user.proto.v1.UserResponse;

import jakarta.validation.Valid;

import org.checkerframework.checker.units.qual.C;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserGatewayService userGatewayService;

    public UserController(UserGatewayService userGatewayService) {
        this.userGatewayService = userGatewayService;
    }

    @GetMapping("/{id}")
    @RequireRoles({ "ADMIN", "COACH", "TALENT" })
    public ResponseEntity<UserResponseDto> findUserById(@PathVariable Long id) {
        UserResponse userResp = userGatewayService.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + id + " not found"));
        return ResponseEntity.ok(UserResponseDto.fromProto(userResp));
    }

    @RequireRoles({ "ADMIN" })
    @PostMapping()
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserDto data) {
        UserResponse createdUserResp = userGatewayService.createUser(data);
        UserResponseDto entity = UserResponseDto.fromProto(createdUserResp);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

}
