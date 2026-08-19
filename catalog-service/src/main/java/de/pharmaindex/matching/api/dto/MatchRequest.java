package de.pharmaindex.matching.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatchRequest(
        @NotBlank @Size(max = 255) String query,
        @Size(max = 16) String atcCode
) {
}
