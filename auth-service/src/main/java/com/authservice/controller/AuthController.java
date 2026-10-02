package com.authservice.controller;

import com.authservice.client.AdminAuthClient;
import com.authservice.client.CustomerAuthClient;
import com.authservice.dto.AdminAuthRequest;
import com.authservice.dto.AuthUserResponse;
import com.authservice.dto.CustomerAuthRequest;
import com.authservice.dto.LoginRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerAuthClient customerAuthClient;
    private final AdminAuthClient adminAuthClient;
    private final JwtUtil jwtUtil;

    @PostMapping("/customer/login")
    public ResponseEntity<TokenResponse> customerLogin(@Valid @RequestBody LoginRequest request) {
        AuthUserResponse user = customerAuthClient.authenticate(
                new CustomerAuthRequest(request.getUsername(), request.getPassword()));
        return ResponseEntity.ok(TokenResponse.builder()
                .token(jwtUtil.generateToken(user.getUsername(), user.getRole()))
                .build());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<TokenResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        AuthUserResponse user = adminAuthClient.authenticate(
                new AdminAuthRequest(request.getUsername(), request.getPassword()));
        return ResponseEntity.ok(TokenResponse.builder()
                .token(jwtUtil.generateToken(user.getUsername(), user.getRole()))
                .build());
    }
}
