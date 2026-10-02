package com.cognizant.customerservice.controller;

import com.cognizant.customerservice.dto.CustomerRequestDTO;
import com.cognizant.customerservice.dto.CustomerResponseDTO;
import com.cognizant.customerservice.exception.ErrorResponse;
import com.cognizant.customerservice.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing customer registration
 * endpoints.
 */
@RestController
@RequestMapping("/api/v1.0/customer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer", description = "Customer Registration APIs")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer",
            description = "Registers a new customer in the system after validating "
                    + "email uniqueness and employer details business rules. The password "
                    + "is BCrypt-hashed before storage and can be used to log in afterward.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CustomerResponseDTO> registerCustomer(
            @Valid @RequestBody CustomerRequestDTO requestDTO) {
        log.info("Received customer registration request for email: {}", requestDTO.getEmail());
        CustomerResponseDTO responseDTO = customerService.registerCustomer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    

}
