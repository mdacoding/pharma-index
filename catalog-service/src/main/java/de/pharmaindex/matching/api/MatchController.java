package de.pharmaindex.matching.api;

import de.pharmaindex.matching.MatchResponse;
import de.pharmaindex.matching.ProductMatcher;
import de.pharmaindex.matching.api.dto.MatchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/match")
@Tag(name = "Matching", description = "Fuzzy-Suche mit Trigramm-Index, Levenshtein und nachvollziehbarem Score")
public class MatchController {

    private final ProductMatcher productMatcher;

    public MatchController(ProductMatcher productMatcher) {
        this.productMatcher = productMatcher;
    }

    @PostMapping
    @Operation(summary = "Ähnliche Präparate zu einem Freitext finden")
    public MatchResponse match(@Valid @RequestBody MatchRequest request) {
        return productMatcher.matchDetailed(request.query(), request.atcCode());
    }
}
