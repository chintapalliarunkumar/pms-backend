package com.cognizant.policyservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.cognizant.policyservice.dto.PolicyPageResponseDTO;
import com.cognizant.policyservice.dto.PolicyRequestDTO;
import com.cognizant.policyservice.dto.PolicyResponseDTO;
import com.cognizant.policyservice.dto.PolicySearchCriteria;
import com.cognizant.policyservice.dto.SearchResponseDTO;
import com.cognizant.policyservice.entity.Policy;
import com.cognizant.policyservice.entity.PolicyType;
import com.cognizant.policyservice.entity.UserType;
import com.cognizant.policyservice.exception.InvalidSearchCriteriaException;
import com.cognizant.policyservice.repository.PolicyRepository;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private PolicyRequestDTO requestDTO;
    
    private Pageable pageable;

    @BeforeEach
    void setUp() {

        pageable = PageRequest.of(0, 10);

        requestDTO = PolicyRequestDTO.builder()
                .policyName("Family Health Shield")
                .startDate(LocalDate.now().plusDays(1))
                .durationYears(5)
                .companyName("Cognizant Insurance Ltd")
                .initialDeposit(BigDecimal.valueOf(10000))
                .policyType(PolicyType.HEALTH_INSURANCE)
                .userTypes(List.of(UserType.A, UserType.B))
                .termsPerYear(12)
                .termAmount(BigDecimal.valueOf(1000))
                .interest(BigDecimal.valueOf(10))
                .build();
    }
    
    @Test
    void getAllPolicies_returnsPagedPolicies() {

        Policy policy = buildPolicy(
                "HI-2026-001",
                "Family Health Shield",
                10,
                "Cognizant Insurance Ltd",
                PolicyType.HEALTH_INSURANCE);

        when(policyRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(policy), pageable, 1));

        PolicyPageResponseDTO result =
                policyService.getAllPolicies(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getPolicies().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalPages());

        assertEquals("HI-2026-001",
                result.getPolicies().get(0).getPolicyId());
    }

    @Test
    void registerPolicy_generatesFirstSequenceWhenNoPriorPolicyExists() {
        when(policyRepository.findTopByPolicyIdStartingWithOrderByPolicyIdDesc(anyString()))
                .thenReturn(Optional.empty());
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(policyRepository.findAll()).thenReturn(List.of());

        PolicyResponseDTO response = policyService.registerPolicy(requestDTO);

        int expectedYear = requestDTO.getStartDate().getYear();
        assertEquals("HI-" + expectedYear + "-001", response.getPolicyId());
    }

    @Test
    void registerPolicy_incrementsSequenceWhenPriorPolicyExists() {
        int year = requestDTO.getStartDate().getYear();
        Policy lastPolicy = Policy.builder().policyId("HI-" + year + "-005").build();

        when(policyRepository.findTopByPolicyIdStartingWithOrderByPolicyIdDesc(anyString()))
                .thenReturn(Optional.of(lastPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(policyRepository.findAll()).thenReturn(List.of());

        PolicyResponseDTO response = policyService.registerPolicy(requestDTO);

        assertEquals("HI-" + year + "-006", response.getPolicyId());
    }

    @Test
    void registerPolicy_calculatesMaturityAmountCorrectly() {
        when(policyRepository.findTopByPolicyIdStartingWithOrderByPolicyIdDesc(anyString()))
                .thenReturn(Optional.empty());
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(policyRepository.findAll()).thenReturn(List.of());

        // initialDeposit=10000, duration=5, termsPerYear=12, termAmount=1000, interest=10%
        // totalContribution = 5 * 12 * 1000 = 60000
        // interestAmount = 60000 * 10 / 100 = 6000
        // maturityAmount = 10000 + 60000 + 6000 = 76000
        PolicyResponseDTO response = policyService.registerPolicy(requestDTO);

        assertEquals(BigDecimal.valueOf(76000).setScale(2), response.getMaturityAmount());
    }

    @Test
    void registerPolicy_calculatesEndDateCorrectly() {
        when(policyRepository.findTopByPolicyIdStartingWithOrderByPolicyIdDesc(anyString()))
                .thenReturn(Optional.empty());
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(policyRepository.findAll()).thenReturn(List.of());

        PolicyResponseDTO response = policyService.registerPolicy(requestDTO);

        assertEquals(requestDTO.getStartDate().plusYears(5), response.getEndDate());
    }

    // ---- searchPolicies (US_03) ----

    private Policy buildPolicy(String policyId, String policyName, int years, String companyName,
                                 PolicyType policyType) {
        return Policy.builder()
                .policyId(policyId)
                .policyName(policyName)
                .durationYears(years)
                .companyName(companyName)
                .policyType(policyType)
                .userTypes(List.of())
                .build();
    }

    @Test
    void searchPolicies_throwsExceptionWhenNoCriteriaSupplied() {

        PolicySearchCriteria emptyCriteria =
                PolicySearchCriteria.builder().build();

        assertThrows(
                InvalidSearchCriteriaException.class,() -> policyService.searchPolicies(emptyCriteria,pageable));
    }

    @Test
    void searchPolicies_filtersByPolicyTypeOnly() {

        when(policyRepository.findAll()).thenReturn(List.of(
                buildPolicy(
                        "HI-2026-001",
                        "Family Health Shield",
                        10,
                        "Cognizant Insurance Ltd",
                        PolicyType.HEALTH_INSURANCE),
                buildPolicy(
                        "TI-2026-001",
                        "Global Travel Cover",
                        5,
                        "Acme Travel Corp",
                        PolicyType.TRAVEL_INSURANCE),
                buildPolicy(
                        "HI-2026-002",
                        "Senior Health Plus",
                        10,
                        "Cognizant Insurance Ltd",
                        PolicyType.HEALTH_INSURANCE)));

        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .policyType(PolicyType.HEALTH_INSURANCE)
                .build();

        SearchResponseDTO response =
                policyService.searchPolicies(criteria, pageable);

        assertEquals(2, response.getPolicies().size());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
    }

    @Test
    void searchPolicies_filtersByPolicyIdExactMatch() {

        when(policyRepository.findAll()).thenReturn(List.of(
                buildPolicy(
                        "HI-2026-001",
                        "Family Health Shield",
                        10,
                        "Cognizant Insurance Ltd",
                        PolicyType.HEALTH_INSURANCE),
                buildPolicy(
                        "TI-2026-001",
                        "Global Travel Cover",
                        5,
                        "Acme Travel Corp",
                        PolicyType.TRAVEL_INSURANCE)));

        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .policyId("HI-2026-001")
                .build();

        SearchResponseDTO response =
                policyService.searchPolicies(criteria, pageable);

        assertEquals(1, response.getPolicies().size());
        assertEquals(
                "HI-2026-001",
                response.getPolicies().get(0).getPolicyId());

        assertEquals(1, response.getTotalElements());
    }
    @Test
    void searchPolicies_combinesMultipleCriteriaWithAndSemantics() {

        when(policyRepository.findAll()).thenReturn(List.of(
                buildPolicy(
                        "HI-2026-001",
                        "Family Health Shield",
                        10,
                        "Cognizant Insurance Ltd",
                        PolicyType.HEALTH_INSURANCE),
                buildPolicy(
                        "HI-2026-002",
                        "Senior Health Plus",
                        10,
                        "Cognizant Insurance Ltd",
                        PolicyType.HEALTH_INSURANCE)));

        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .policyType(PolicyType.HEALTH_INSURANCE)
                .policyName("Senior")
                .build();

        SearchResponseDTO response =
                policyService.searchPolicies(criteria, pageable);

        assertEquals(1, response.getPolicies().size());
        assertEquals(
                "HI-2026-002",
                response.getPolicies().get(0).getPolicyId());

        assertEquals(1, response.getTotalElements());
    }

    @Test
    void searchPolicies_returnsFriendlyMessageWhenNoMatch() {

        when(policyRepository.findAll()).thenReturn(List.of(
                buildPolicy(
                        "HI-2026-001",
                        "Family Health Shield",
                        10,
                        "Cognizant Insurance Ltd",
                        PolicyType.HEALTH_INSURANCE)));

        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .companyName("NonExistentCompany")
                .build();

        SearchResponseDTO response =
                policyService.searchPolicies(criteria, pageable);

        assertTrue(response.getPolicies().isEmpty());

        assertEquals(
                "No policies found matching your search criteria. Please try different search parameters.",
                response.getMessage());

        assertEquals(0, response.getTotalElements());
    }

    @Test
    void searchPolicies_filtersByCompanyNamePartialCaseInsensitiveMatch() {

        when(policyRepository.findAll()).thenReturn(List.of(
                buildPolicy(
                        "HI-2026-001",
                        "Family Health Shield",
                        10,
                        "Cognizant Insurance Ltd",
                        PolicyType.HEALTH_INSURANCE),
                buildPolicy(
                        "TI-2026-001",
                        "Global Travel Cover",
                        5,
                        "Acme Travel Corp",
                        PolicyType.TRAVEL_INSURANCE)));

        PolicySearchCriteria criteria = PolicySearchCriteria.builder()
                .companyName("cognizant")
                .build();

        SearchResponseDTO response =
                policyService.searchPolicies(criteria, pageable);

        assertEquals(1, response.getPolicies().size());
        assertEquals(1, response.getTotalElements());
    }
}
