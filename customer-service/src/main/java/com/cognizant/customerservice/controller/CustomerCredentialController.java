package com.cognizant.customerservice.controller;

import com.cognizant.customerservice.dto.AuthUserResponseDTO;
import com.cognizant.customerservice.dto.LoginRequestDTO;
import com.cognizant.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/service/customer")
@RequiredArgsConstructor
public class CustomerCredentialController {
    private final CustomerService customerService;

    @PostMapping("/verify")
    public ResponseEntity<AuthUserResponseDTO> verify(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(customerService.verifyCredentials(request));
    }
}
