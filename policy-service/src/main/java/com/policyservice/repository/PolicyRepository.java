package com.policyservice.repository;

import com.policyservice.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Policy entity operations.
 */
public interface PolicyRepository extends JpaRepository<Policy, String> {

    Optional<Policy> findTopByPolicyIdStartingWithOrderByPolicyIdDesc(String prefix);
}
