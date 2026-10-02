package com.cognizant.policyservice.service;

import org.springframework.data.domain.Pageable;

import com.cognizant.policyservice.dto.PolicyPageResponseDTO;
import com.cognizant.policyservice.dto.PolicyRequestDTO;
import com.cognizant.policyservice.dto.PolicyResponseDTO;
import com.cognizant.policyservice.dto.PolicySearchCriteria;
import com.cognizant.policyservice.dto.SearchResponseDTO;

/**
 * Service contract for policy-related business operations.
 */
public interface PolicyService {

    PolicyResponseDTO registerPolicy(PolicyRequestDTO requestDTO);

    PolicyPageResponseDTO getAllPolicies(Pageable pageable);

    SearchResponseDTO searchPolicies(PolicySearchCriteria criteria,Pageable pageable);
}
