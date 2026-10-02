package com.cognizant.policyservice.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated policy response")
public class PolicyPageResponseDTO {

	private List<PolicyResponseDTO> policies;
	
	private int currentPage;

	private int pageSize;

	private long totalElements;

	private int totalPages;

	private String sortBy;

	private String sortDirection;
}