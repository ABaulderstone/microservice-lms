package com.example.api_gateway.auth;

import com.example.api_gateway.auth.dtos.LoginRequestDto;
import com.example.api_gateway.auth.dtos.LoginResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthGatewayService authGatewayService;

    public AuthController(AuthGatewayService authGatewayService) {
        this.authGatewayService = authGatewayService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request) {
        var responseMap = authGatewayService.login(request);
        return ResponseEntity.ok(new LoginResponseDto(responseMap.get("token")));
    }
}
