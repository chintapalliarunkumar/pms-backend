package com.policyservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an insurance Policy created by an admin.
 * policyId is a business-generated identifier (not a DB auto-increment)
 * in the format <SHORTCODE>-<YEAR>-XXX, e.g. HI-2026-001.
 */
@Entity
@Table(name = "policies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @Column(name = "policy_id", length = 20)
    private String policyId;

    @Column(name = "policy_name", nullable = false, length = 150)
    private String policyName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "initial_deposit", nullable = false, precision = 15, scale = 2)
    private BigDecimal initialDeposit;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 30)
    private PolicyType policyType;

    @Column(name = "terms_per_year", nullable = false)
    private Integer termsPerYear;

    @Column(name = "term_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal termAmount;

    @Column(name = "interest", nullable = false, precision = 5, scale = 2)
    private BigDecimal interest;

    @Column(name = "maturity_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maturityAmount;

    /**
     * Unidirectional one-to-many relationship: the foreign key (policy_id)
     * lives on the policy_user_types table. PolicyUserType remains a flat
     * entity (id, policyId, userType) as per the data model.
     */
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "policy_id", referencedColumnName = "policy_id")
    private List<PolicyUserType> userTypes = new ArrayList<>();

    /**
     * Convenience method to attach a user type to this policy.
     */
    public void addUserType(PolicyUserType policyUserType) {
        this.userTypes.add(policyUserType);
    }
}
