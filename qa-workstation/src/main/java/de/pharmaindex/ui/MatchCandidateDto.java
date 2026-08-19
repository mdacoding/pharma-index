package de.pharmaindex.ui;

import java.util.List;

public record MatchCandidateDto(
        long productId,
        String pzn,
        String name,
        String manufacturer,
        String atcCode,
        double score,
        double trigram,
        double levenshtein,
        double tokenOverlap,
        List<String> explanations
) {
}
