package com.policyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wraps search results with a user-friendly message, per US_03's
 * acceptance criteria: "If no policies found for the user display user
 * friendly message." The HTTP status is still 200 either way - an empty
 * result set is a successful search, not an error.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search results with a friendly summary message")
public class SearchResponseDTO {

    @Schema(description = "Human-readable summary of the search outcome",
            example = "2 policy(ies) found matching your search criteria.")
    private String message;

    @Schema(description = "Matching policies with complete details (empty if none found)")
    private List<PolicyResponseDTO> policies;
    
    private int currentPage;

    private int pageSize;
    private long totalElements;
    private int totalPages;
}
