package com.policyservice.controller;

import com.policyservice.dto.AdminLoginRequestDTO;
import com.policyservice.dto.AuthUserResponseDTO;
import com.policyservice.exception.InvalidCredentialsException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service/admin")
public class AdminCredentialController {
    @Value("${admin.default.username:admin}")
    private String adminUsername;

    @Value("${admin.default.password:admin}")
    private String adminPassword;

    @PostMapping("/verify")
    public ResponseEntity<AuthUserResponseDTO> verify(@Valid @RequestBody AdminLoginRequestDTO request) {
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        return ResponseEntity.ok(new AuthUserResponseDTO(adminUsername, "ADMIN"));
    }
}
