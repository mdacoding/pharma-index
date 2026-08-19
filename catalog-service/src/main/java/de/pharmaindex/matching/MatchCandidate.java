package de.pharmaindex.matching;

import java.util.List;

public record MatchCandidate(
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
