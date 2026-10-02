package com.policyservice.service;

import org.springframework.data.domain.Pageable;

import com.policyservice.dto.PolicyPageResponseDTO;
import com.policyservice.dto.PolicyRequestDTO;
import com.policyservice.dto.PolicyResponseDTO;
import com.policyservice.dto.PolicySearchCriteria;
import com.policyservice.dto.SearchResponseDTO;

/**
 * Service contract for policy-related business operations.
 */
public interface PolicyService {

    PolicyResponseDTO registerPolicy(PolicyRequestDTO requestDTO);

    PolicyPageResponseDTO getAllPolicies(Pageable pageable);

    SearchResponseDTO searchPolicies(PolicySearchCriteria criteria,Pageable pageable);
}
