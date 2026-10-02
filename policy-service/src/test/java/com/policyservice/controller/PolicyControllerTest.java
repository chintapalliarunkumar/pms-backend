package com.policyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policyservice.dto.*;
import com.policyservice.entity.PolicyType;
import com.policyservice.entity.UserType;
import com.policyservice.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PolicyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyService policyService;

    private PolicyRequestDTO requestDTO;
    private PolicyResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = PolicyRequestDTO.builder()
                .policyName("Family Health Shield")
                .startDate(LocalDate.now().plusDays(1))
                .durationYears(10)
                .companyName("Cognizant Insurance Ltd")
                .initialDeposit(BigDecimal.valueOf(50000))
                .policyType(PolicyType.HEALTH_INSURANCE)
                .userTypes(List.of(UserType.A, UserType.B))
                .termsPerYear(12)
                .termAmount(BigDecimal.valueOf(2500))
                .interest(BigDecimal.valueOf(6.5))
                .build();

        responseDTO = PolicyResponseDTO.builder()
                .policyId("HI-2026-001")
                .policyName("Family Health Shield")
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getStartDate().plusYears(10))
                .durationYears(10)
                .companyName("Cognizant Insurance Ltd")
                .initialDeposit(BigDecimal.valueOf(50000))
                .policyType(PolicyType.HEALTH_INSURANCE)
                .userTypes(List.of(UserType.A, UserType.B))
                .termsPerYear(12)
                .termAmount(BigDecimal.valueOf(2500))
                .interest(BigDecimal.valueOf(6.5))
                .maturityAmount(BigDecimal.valueOf(80000))
                .confirmationMessage("Policy registered successfully")
                .build();
    }

    @Test
    void registerPolicy_success() throws Exception {
        when(policyService.registerPolicy(any(PolicyRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1.0/policy/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.policyId").value("HI-2026-001"))
                .andExpect(jsonPath("$.policyName").value("Family Health Shield"));

        verify(policyService).registerPolicy(any(PolicyRequestDTO.class));
    }

    @Test
    void registerPolicy_validationError() throws Exception {
        requestDTO.setPolicyName("");

        mockMvc.perform(post("/api/v1.0/policy/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllPolicies_success() throws Exception {
        PolicyPageResponseDTO pageResponse = PolicyPageResponseDTO.builder()
                .policies(List.of(responseDTO))
                .currentPage(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .sortBy("policyId")
                .sortDirection("DESC")
                .build();

        when(policyService.getAllPolicies(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1.0/policy/getall")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policies[0].policyId").value("HI-2026-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchPolicies_success() throws Exception {
        SearchResponseDTO searchResponse = SearchResponseDTO.builder()
                .message("1 policy(ies) found matching your search criteria.")
                .policies(List.of(responseDTO))
                .currentPage(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(policyService.searchPolicies(any(PolicySearchCriteria.class), any())).thenReturn(searchResponse);

        mockMvc.perform(get("/api/v1.0/policy/searches")
                        .param("policyType", "HEALTH_INSURANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("1 policy(ies) found matching your search criteria."))
                .andExpect(jsonPath("$.policies[0].policyId").value("HI-2026-001"));
    }

    @Test
    void searchPolicies_noResults() throws Exception {
        SearchResponseDTO emptyResponse = SearchResponseDTO.builder()
                .message("No policies found matching your search criteria.")
                .policies(List.of())
                .currentPage(0)
                .pageSize(10)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(policyService.searchPolicies(any(PolicySearchCriteria.class), any())).thenReturn(emptyResponse);

        mockMvc.perform(get("/api/v1.0/policy/searches")
                        .param("policyName", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policies").isEmpty());
    }
}
