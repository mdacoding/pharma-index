package de.pharmaindex.matching;

import java.util.List;

public record MatchResponse(
        String query,
        int candidatePoolSize,
        long durationMs,
        List<MatchCandidate> matches
) {
}
