package com.cognizant.authservice.controller;

import com.cognizant.authservice.client.AdminAuthClient;
import com.cognizant.authservice.client.CustomerAuthClient;
import com.cognizant.authservice.dto.AdminAuthRequest;
import com.cognizant.authservice.dto.AuthUserResponse;
import com.cognizant.authservice.dto.CustomerAuthRequest;
import com.cognizant.authservice.dto.LoginRequest;
import com.cognizant.authservice.dto.TokenResponse;
import com.cognizant.authservice.security.JwtUtil;
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
