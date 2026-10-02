package com.cognizant.customerservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for authenticating an existing customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to log in and obtain a JWT token")
public class LoginRequestDTO {

    @NotBlank(message = "Email is mandatory")
    @Schema(description = "Login email (same as used at registration)", example = "john.smith@example.com")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Schema(description = "Plain-text password", example = "Str0ngPass!")
    private String password;
}
