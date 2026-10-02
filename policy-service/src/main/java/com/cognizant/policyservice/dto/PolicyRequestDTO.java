package com.cognizant.policyservice.dto;

import com.cognizant.policyservice.entity.PolicyType;
import com.cognizant.policyservice.entity.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request payload for registering a new policy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to register a new policy")
public class PolicyRequestDTO {

    @NotBlank(message = "Policy name is mandatory")
    @Size(max = 150, message = "Policy name must not exceed 150 characters")
    @Schema(description = "Name of the policy", example = "Family Health Shield")
    private String policyName;

    @NotNull(message = "Start date is mandatory")
    @FutureOrPresent(message = "Policy start date must be on or after the current date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Start date in YYYY-MM-DD format", example = "2026-09-01")
    private LocalDate startDate;

    @NotNull(message = "Duration in years is mandatory")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 100, message = "Duration must not exceed 100 years")
    @Schema(description = "Duration of the policy in years", example = "10")
    private Integer durationYears;

    @NotBlank(message = "Company name is mandatory")
    @Size(max = 150, message = "Company name must not exceed 150 characters")
    @Schema(description = "Company name offering the policy", example = "Cognizant Insurance Ltd")
    private String companyName;

    @NotNull(message = "Initial deposit is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial deposit must be greater than zero")
    @Schema(description = "Amount to be deposited at start", example = "50000")
    private BigDecimal initialDeposit;

    @NotNull(message = "Policy type is mandatory")
    @Schema(description = "Type of the policy", example = "HEALTH_INSURANCE")
    private PolicyType policyType;

    @NotEmpty(message = "At least one user type must be selected")
    @Schema(description = "User types eligible for this policy", example = "[\"A\", \"B\", \"C\"]")
    private List<UserType> userTypes;

    @NotNull(message = "Terms per year is mandatory")
    @Min(value = 1, message = "Terms per year must be at least 1")
    @Schema(description = "Number of payment terms per year", example = "12")
    private Integer termsPerYear;

    @NotNull(message = "Term amount is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Term amount must be greater than zero")
    @Schema(description = "One term's payment amount", example = "2500")
    private BigDecimal termAmount;

    @NotNull(message = "Interest is mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Interest must not be negative")
    @Schema(description = "Interest rate for the policy (percentage)", example = "6.5")
    private BigDecimal interest;
}
