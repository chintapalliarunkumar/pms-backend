package com.policyservice.dto;

import com.policyservice.entity.PolicyType;
import com.policyservice.entity.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response payload returned after policy registration or lookup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Policy details returned by the API")
public class PolicyResponseDTO {

    private String policyId;
    private String policyName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer durationYears;
    private String companyName;
    private BigDecimal initialDeposit;
    private PolicyType policyType;
    private List<UserType> userTypes;
    private Integer termsPerYear;
    private BigDecimal termAmount;
    private BigDecimal interest;
    private BigDecimal maturityAmount;

    @Schema(description = "HTML-formatted confirmation message shown after successful registration")
    private String confirmationMessage;
}
