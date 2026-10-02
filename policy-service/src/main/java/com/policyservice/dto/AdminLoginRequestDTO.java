package com.policyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for authenticating an existing admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to authenticate an admin and obtain a JWT token")
public class AdminLoginRequestDTO {

    @NotBlank(message = "Username is mandatory")
    @Schema(description = "Admin login username", example = "admin")
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Schema(description = "Plain-text password", example = "admin123")
    private String password;
}
