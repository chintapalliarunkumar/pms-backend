package com.policyservice.dto;

import com.policyservice.entity.PolicyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search criteria - at least one field must be provided")
public class PolicySearchCriteria {

    @Schema(description = "Type of the policy", example = "HEALTH_INSURANCE")
    private PolicyType policyType;

    @Schema(description = "Duration of the policy in years", example = "10")
    private Integer years;

    @Schema(description = "Company name (partial, case-insensitive match)", example = "Cognizant")
    private String companyName;

    @Schema(description = "Exact policy ID", example = "HI-2026-001")
    private String policyId;

    @Schema(description = "Policy name (partial, case-insensitive match)", example = "Family Health")
    private String policyName;

    public boolean hasAnyCriteria() {
        return policyType != null
                || years != null
                || (companyName != null && !companyName.isBlank())
                || (policyId != null && !policyId.isBlank())
                || (policyName != null && !policyName.isBlank());
    }
}
