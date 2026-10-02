package com.cognizant.customerservice.dto;

import com.cognizant.customerservice.entity.EmployerType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for registering a new customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to register a new customer")
public class CustomerRequestDTO {

    @NotBlank(message = "First name is mandatory")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Name of the user", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Name of the father", example = "Smith")
    private String lastName;

    @NotNull(message = "Date of birth is mandatory")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "User DOB in YYYY-MM-DD format", example = "1990-05-15")
    private LocalDate dob;

    @NotBlank(message = "Address is mandatory")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(description = "Address of the user", example = "12 MG Road, Chennai")
    private String address;

    @NotBlank(message = "Contact number is mandatory")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be a valid 10-digit number")
    @Schema(description = "Contact number of the applicant", example = "9876543210")
    private String contactNo;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Schema(description = "Email address of the user (also used as the login username)", example = "john.smith@example.com")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "Plain-text password (will be BCrypt-hashed before storage; used to log in later)", example = "Str0ngPass!")
    private String password;

    @NotNull(message = "Salary is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than zero")
    @Schema(description = "Salary per year (in rupees)", example = "800000")
    private BigDecimal salary;

    @NotBlank(message = "PAN number is mandatory")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number must be in valid format e.g. ABCDE1234F")
    @Schema(description = "PAN card number", example = "ABCDE1234F")
    private String panNo;

    @NotNull(message = "Employer type is mandatory")
    @Schema(description = "Type of the employer", example = "SALARIED")
    private EmployerType employerType;

    @Size(max = 150, message = "Employer name must not exceed 150 characters")
    @Schema(description = "Name of the employer, mandatory if SALARIED, must be null if SELF_EMPLOYED", example = "Acme Corp")
    private String employerName;
}
