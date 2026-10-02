package com.cognizant.policyservice.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cognizant.policyservice.dto.PolicyPageResponseDTO;
import com.cognizant.policyservice.dto.PolicyRequestDTO;
import com.cognizant.policyservice.dto.PolicyResponseDTO;
import com.cognizant.policyservice.dto.PolicySearchCriteria;
import com.cognizant.policyservice.dto.SearchResponseDTO;
import com.cognizant.policyservice.entity.Policy;
import com.cognizant.policyservice.entity.PolicyType;
import com.cognizant.policyservice.entity.PolicyUserType;
import com.cognizant.policyservice.entity.UserType;
import com.cognizant.policyservice.exception.InvalidPolicyDataException;
import com.cognizant.policyservice.exception.InvalidSearchCriteriaException;
import com.cognizant.policyservice.repository.PolicyRepository;
import com.cognizant.policyservice.service.PolicyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link PolicyService} handling policy registration, policy
 * ID generation, maturity amount calculation, and retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyServiceImpl implements PolicyService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
	private static final int SEQUENCE_WIDTH = 3;

	private final PolicyRepository policyRepository;

	@Override
	@Transactional
	public PolicyResponseDTO registerPolicy(PolicyRequestDTO requestDTO) {
		log.info("Registering policy of type: {}", requestDTO.getPolicyType());

		validatePolicyRequest(requestDTO);

		LocalDate endDate = calculateEndDate(requestDTO.getStartDate(), requestDTO.getDurationYears());
		BigDecimal maturityAmount = calculateMaturityAmount(requestDTO.getInitialDeposit(),
				requestDTO.getDurationYears(), requestDTO.getTermsPerYear(), requestDTO.getTermAmount(),
				requestDTO.getInterest());

		String policyId = generatePolicyId(requestDTO.getPolicyType(), requestDTO.getStartDate());

		Policy policy = Policy.builder().policyId(policyId).policyName(requestDTO.getPolicyName())
				.startDate(requestDTO.getStartDate()).endDate(endDate).durationYears(requestDTO.getDurationYears())
				.companyName(requestDTO.getCompanyName()).initialDeposit(requestDTO.getInitialDeposit())
				.policyType(requestDTO.getPolicyType()).termsPerYear(requestDTO.getTermsPerYear())
				.termAmount(requestDTO.getTermAmount()).interest(requestDTO.getInterest())
				.maturityAmount(maturityAmount).build();

		requestDTO.getUserTypes()
				.forEach(userType -> policy.addUserType(PolicyUserType.builder().userType(userType).build()));

		Policy savedPolicy = policyRepository.save(policy);
		log.info("Policy registered successfully with id: {}", savedPolicy.getPolicyId());

		return mapToResponseDTO(savedPolicy, buildConfirmationMessage(savedPolicy));
	}

	@Override
	public PolicyPageResponseDTO getAllPolicies(Pageable pageable) {

		Page<PolicyResponseDTO> page = policyRepository.findAll(pageable).map(policy -> mapToResponseDTO(policy, null));

		String sortBy = pageable.getSort().stream().findFirst().map(order -> order.getProperty()).orElse("policyId");

		String sortDirection = pageable.getSort().stream().findFirst().map(order -> order.getDirection().name())
				.orElse("DESC");

		return PolicyPageResponseDTO.builder()
				.policies(page.getContent())
				.currentPage(page.getNumber())
				.pageSize(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages()).sortBy(sortBy)
				.sortDirection(sortDirection)
				.build();
	}

	@Override
	public SearchResponseDTO searchPolicies(PolicySearchCriteria criteria, Pageable pageable) {

		if (!criteria.hasAnyCriteria()) {
			throw new InvalidSearchCriteriaException(
					"At least one search criterion (policyType, years, companyName, policyId, "
							+ "or policyName) must be provided");
		}

		List<PolicyResponseDTO> filteredPolicies = policyRepository.findAll().stream()
				.filter(policy -> matchesPolicyType(policy, criteria)).filter(policy -> matchesYears(policy, criteria))
				.filter(policy -> matchesCompanyName(policy, criteria))
				.filter(policy -> matchesPolicyId(policy, criteria))
				.filter(policy -> matchesPolicyName(policy, criteria)).map(policy -> mapToResponseDTO(policy, null))
				.toList();

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filteredPolicies.size());

		List<PolicyResponseDTO> pagedPolicies = start >= filteredPolicies.size() ? List.of()
				: filteredPolicies.subList(start, end);

		Page<PolicyResponseDTO> page = new PageImpl<>(pagedPolicies, pageable, filteredPolicies.size());

		log.info("Search returned {} of {} policies for criteria: {}", filteredPolicies.size(),
				policyRepository.count(), criteria);

		return SearchResponseDTO.builder().message(buildFriendlyMessage(filteredPolicies.size()))
				.policies(page.getContent()).currentPage(page.getNumber()).pageSize(page.getSize())
				.totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).build();
	}

	private String buildFriendlyMessage(int resultCount) {
		if (resultCount == 0) {
			return "No policies found matching your search criteria. " + "Please try different search parameters.";
		}
		return resultCount == 1 ? "1 policy found matching your search criteria."
				: resultCount + " policies found matching your search criteria.";
	}

	private boolean matchesPolicyType(Policy policy, PolicySearchCriteria criteria) {
		return criteria.getPolicyType() == null || Objects.equals(policy.getPolicyType(), criteria.getPolicyType());
	}

	private boolean matchesYears(Policy policy, PolicySearchCriteria criteria) {
		return criteria.getYears() == null || Objects.equals(policy.getDurationYears(), criteria.getYears());
	}

	private boolean matchesCompanyName(Policy policy, PolicySearchCriteria criteria) {
		return isBlank(criteria.getCompanyName())
				|| containsIgnoreCase(policy.getCompanyName(), criteria.getCompanyName());
	}

	private boolean matchesPolicyId(Policy policy, PolicySearchCriteria criteria) {
		return isBlank(criteria.getPolicyId()) || equalsIgnoreCase(policy.getPolicyId(), criteria.getPolicyId());
	}

	private boolean matchesPolicyName(Policy policy, PolicySearchCriteria criteria) {
		return isBlank(criteria.getPolicyName())
				|| containsIgnoreCase(policy.getPolicyName(), criteria.getPolicyName());
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private boolean containsIgnoreCase(String source, String target) {
		return source != null && source.toLowerCase().contains(target.toLowerCase());
	}

	private boolean equalsIgnoreCase(String source, String target) {
		return source != null && source.equalsIgnoreCase(target);
	}

	/**
	 * Validates business rules that go beyond simple field-level bean validation.
	 */
	private void validatePolicyRequest(PolicyRequestDTO requestDTO) {
		if (requestDTO.getStartDate().isBefore(LocalDate.now())) {
			throw new InvalidPolicyDataException("Policy start date should be on or after the current date");
		}
		if (requestDTO.getPolicyType() == null) {
			throw new InvalidPolicyDataException("Policy type needs to be selected");
		}
		if (requestDTO.getUserTypes() == null || requestDTO.getUserTypes().isEmpty()) {
			throw new InvalidPolicyDataException("User types needs to be selected");
		}
	}

	/**
	 * Calculates the policy end date based on the start date and duration in years.
	 */
	private LocalDate calculateEndDate(LocalDate startDate, int durationYears) {
		return startDate.plusYears(durationYears);
	}

	private BigDecimal calculateMaturityAmount(BigDecimal initialDeposit, int durationYears, int termsPerYear,
			BigDecimal termAmount, BigDecimal interest) {
		BigDecimal totalTerms = BigDecimal.valueOf((long) durationYears * termsPerYear);
		BigDecimal totalContribution = totalTerms.multiply(termAmount);
		BigDecimal interestAmount = totalContribution.multiply(interest).divide(HUNDRED, 2, RoundingMode.HALF_UP);

		return initialDeposit.add(totalContribution).add(interestAmount).setScale(2, RoundingMode.HALF_UP);
	}

	private String generatePolicyId(PolicyType policyType, LocalDate startDate) {
		String shortCode = policyType.getShortCode();
		int year = Year.from(startDate).getValue();
		String prefix = shortCode + "-" + year + "-";

		int nextSequence = policyRepository.findTopByPolicyIdStartingWithOrderByPolicyIdDesc(prefix)
				.map(this::extractSequence).map(seq -> seq + 1).orElse(1);

		String sequenceStr = String.format("%0" + SEQUENCE_WIDTH + "d", nextSequence);
		return prefix + sequenceStr;
	}

	private int extractSequence(Policy lastPolicy) {
		String lastPolicyId = lastPolicy.getPolicyId();
		String sequencePart = lastPolicyId.substring(lastPolicyId.lastIndexOf('-') + 1);
		try {
			return Integer.parseInt(sequencePart);
		} catch (NumberFormatException e) {
			log.warn("Could not parse sequence from policy id: {}", lastPolicyId);
			return 0;
		}
	}

	private String buildConfirmationMessage(Policy policy) {
		long countForType = policyRepository.findAll().stream().filter(p -> p.getPolicyType() == policy.getPolicyType())
				.count();

		return "<html><body>" + "<p>Dear Admin,</p>" + "<p>The policy is successfully registered.</p>"
				+ "<p>The policy " + policy.getPolicyId() + " is available to the users from " + policy.getStartDate()
				+ " to " + policy.getEndDate() + ".</p>" + "<p>This is the " + countForType + "th policy in the "
				+ policy.getPolicyType() + ". "
				+ "To add more Click <a href=\"/api/v1.0/policy/register\">Policy Registration</a>.</p>"
				+ "</body></html>";
	}

	private PolicyResponseDTO mapToResponseDTO(Policy policy, String confirmationMessage) {
		List<UserType> userTypes = policy.getUserTypes().stream().map(PolicyUserType::getUserType)
				.collect(Collectors.toList());

		return PolicyResponseDTO.builder().policyId(policy.getPolicyId()).policyName(policy.getPolicyName())
				.startDate(policy.getStartDate()).endDate(policy.getEndDate()).durationYears(policy.getDurationYears())
				.companyName(policy.getCompanyName()).initialDeposit(policy.getInitialDeposit())
				.policyType(policy.getPolicyType()).userTypes(userTypes).termsPerYear(policy.getTermsPerYear())
				.termAmount(policy.getTermAmount()).interest(policy.getInterest())
				.maturityAmount(policy.getMaturityAmount()).confirmationMessage(confirmationMessage).build();
	}
}
