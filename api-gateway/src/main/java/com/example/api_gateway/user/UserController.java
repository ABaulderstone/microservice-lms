package com.example.api_gateway.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.api_gateway.user.dtos.UserResponseDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserGatewayService userGatewayService;

    public UserController(UserGatewayService userGatewayService) {
        this.userGatewayService = userGatewayService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getMethodName(@PathVariable Long id) {
        var UserResponse = userGatewayService.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + id + " not found"));
        return ResponseEntity.ok(UserResponseDto.fromProto(UserResponse));
    }

}
