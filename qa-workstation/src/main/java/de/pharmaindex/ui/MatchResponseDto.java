package de.pharmaindex.ui;

import java.util.List;

public record MatchResponseDto(
        String query,
        int candidatePoolSize,
        long durationMs,
        List<MatchCandidateDto> matches
) {
}
