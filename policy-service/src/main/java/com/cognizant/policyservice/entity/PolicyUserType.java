package com.cognizant.policyservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single user-type eligibility record for a policy.
 * A policy can have multiple user types (A-E) associated with it.
 */
@Entity
@Table(name = "policy_user_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyUserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "policy_id", length = 20, insertable = false, updatable = false)
    private String policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 5)
    private UserType userType;
}
