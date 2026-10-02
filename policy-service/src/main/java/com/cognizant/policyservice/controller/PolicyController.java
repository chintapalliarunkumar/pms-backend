package com.cognizant.policyservice.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.policyservice.dto.PolicyPageResponseDTO;
import com.cognizant.policyservice.dto.PolicyRequestDTO;
import com.cognizant.policyservice.dto.PolicyResponseDTO;
import com.cognizant.policyservice.dto.PolicySearchCriteria;
import com.cognizant.policyservice.dto.SearchResponseDTO;
import com.cognizant.policyservice.entity.PolicyType;
import com.cognizant.policyservice.exception.ErrorResponse;
import com.cognizant.policyservice.service.PolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1.0/policy")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Policy", description = "Policy Registration, Retrieval, and Search APIs")
public class PolicyController {

	private final PolicyService policyService;

	@PostMapping("/register")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Register a new policy (ADMIN only)", description = "Registers a new policy, auto-generates the policy ID "
			+ "(<SHORTCODE>-<YEAR>-XXX), calculates the end date and maturity amount. "
			+ "Requires a valid ADMIN bearer token.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Policy registered successfully"),
			@ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	public ResponseEntity<PolicyResponseDTO> registerPolicy(@Valid @RequestBody PolicyRequestDTO requestDTO) {
		log.info("Received policy registration request of type: {}", requestDTO.getPolicyType());
		PolicyResponseDTO responseDTO = policyService.registerPolicy(requestDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
	}

	@GetMapping("/getall")
	@Operation(summary = "Get all policies (ADMIN or CUSTOMER)", description = "Fetches the complete list of registered policies.")
	public ResponseEntity<PolicyPageResponseDTO> getAllPolicies(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("policyId").descending());

		return ResponseEntity.ok(policyService.getAllPolicies(pageable));
	}

	@GetMapping("/searches")
	@Operation(summary = "Search policies (ADMIN or CUSTOMER) - US_03", description = "Searches policies by any combination of policyType, years, companyName, "
			+ "policyId, or policyName. At least one parameter must be supplied. All supplied "
			+ "parameters are combined with AND semantics. Returns complete policy details, "
			+ "with a friendly message if nothing matched.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Search executed successfully "
					+ "(check the message/policies fields - an empty list means no matches)"),
			@ApiResponse(responseCode = "400", description = "No search criteria supplied", content = @Content(schema = @Schema(implementation = ErrorResponse.class))) })
	public ResponseEntity<SearchResponseDTO> searchPolicies(
			@Parameter(description = "Type of the policy") @RequestParam(required = false) PolicyType policyType,
			@Parameter(description = "Duration of the policy in years") @RequestParam(required = false) Integer years,
			@Parameter(description = "Company name (partial match)") @RequestParam(required = false) String companyName,
			@Parameter(description = "Exact policy ID") @RequestParam(required = false) String policyId,
			@Parameter(description = "Policy name (partial match)") @RequestParam(required = false) String policyName,
			@PageableDefault(page = 0, size = 10, sort = "policyId") Pageable pageable) {

		PolicySearchCriteria criteria = PolicySearchCriteria.builder().policyType(policyType).years(years)
				.companyName(companyName).policyId(policyId).policyName(policyName).build();

		log.info("Received search request with criteria: {}", criteria);
		SearchResponseDTO response = policyService.searchPolicies(criteria, pageable);
		return ResponseEntity.ok(response);
	}

}
