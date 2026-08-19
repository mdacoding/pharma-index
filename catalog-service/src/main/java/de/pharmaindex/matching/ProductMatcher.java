package de.pharmaindex.matching;

import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.repo.ProductRepository;
import de.pharmaindex.config.PharmaIndexProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Fuzzy-Match für Freitext aus Warenwirtschaft oder Scan.
 * Erst Kandidaten über {@link TrigramIndex}, dann gewichtetes Scoring inkl. ATC- und Wirkstoff-Boost.
 */
@Service
public class ProductMatcher {

    private final ProductRepository productRepository;
    private final PharmaIndexProperties properties;
    private final MeterRegistry meterRegistry;
    private final TrigramIndex index = new TrigramIndex();

    public ProductMatcher(
            ProductRepository productRepository,
            PharmaIndexProperties properties,
            MeterRegistry meterRegistry
    ) {
        this.productRepository = productRepository;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Transactional(readOnly = true)
    public void rebuild() {
        productRepository.findAll().forEach(this::index);
    }

    public void index(Product product) {
        if (product.getId() != null) {
            index.put(product.getId(), searchable(product));
        }
    }

    public void remove(Product product) {
        if (product.getId() != null) {
            index.remove(product.getId());
        }
    }

    public int indexSize() {
        return index.size();
    }

    public MatchResponse matchDetailed(String query, String atcCode) {
        long started = System.nanoTime();
        Set<Long> pool = new HashSet<>();
        List<MatchCandidate> matches = match(query, atcCode, null, pool);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        meterRegistry.counter("pharmaindex.matching.requests").increment();
        Timer.builder("pharmaindex.matching.duration")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
        return new MatchResponse(query, pool.size(), durationMs, matches);
    }

    public List<MatchCandidate> match(String query, String atcCode, Long excludeProductId) {
        return match(query, atcCode, excludeProductId, new HashSet<>());
    }

    private List<MatchCandidate> match(String query, String atcCode, Long excludeProductId, Set<Long> poolOut) {
        String normalizedQuery = NormalizedText.of(query);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        Set<Long> candidateIds = new HashSet<>(index.candidateIds(normalizedQuery));
        if (candidateIds.isEmpty()) {
            productRepository.findAll().forEach(product -> candidateIds.add(product.getId()));
        }
        poolOut.addAll(candidateIds);
        List<MatchCandidate> ranked = new ArrayList<>();
        for (Long id : candidateIds) {
            if (excludeProductId != null && excludeProductId.equals(id)) {
                continue;
            }
            productRepository.findById(id).ifPresent(product -> {
                MatchCandidate candidate = score(product, normalizedQuery, atcCode);
                if (candidate.score() >= properties.getMatching().getMinScore()) {
                    ranked.add(candidate);
                }
            });
        }
        ranked.sort(Comparator.comparingDouble(MatchCandidate::score).reversed());
        int limit = properties.getMatching().getMaxCandidates();
        return ranked.size() > limit ? ranked.subList(0, limit) : ranked;
    }

    private MatchCandidate score(Product product, String normalizedQuery, String atcCode) {
        String document = searchable(product);
        double trigram = index.dice(product.getId(), normalizedQuery);
        double levenshtein = Levenshtein.similarity(normalizedQuery, NormalizedText.of(product.getName()));
        double tokens = tokenOverlap(normalizedQuery, document);
        double score = (0.45 * trigram) + (0.35 * levenshtein) + (0.20 * tokens);
        List<String> explanations = new ArrayList<>();
        explanations.add("Trigramm-Dice " + pct(trigram));
        explanations.add("Levenshtein " + pct(levenshtein));
        explanations.add("Token-Overlap " + pct(tokens));
        if (atcCode != null && atcCode.equalsIgnoreCase(blankToNull(product.getAtcCode()))) {
            score = Math.min(1.0, score + 0.12);
            explanations.add("ATC-Treffer +12 %");
        }
        String queryIngredient = firstSignificantToken(normalizedQuery);
        if (queryIngredient != null && NormalizedText.of(nullToEmpty(product.getActiveIngredient())).contains(queryIngredient)) {
            score = Math.min(1.0, score + 0.08);
            explanations.add("Wirkstoff-Treffer +8 %");
        }
        return new MatchCandidate(
                product.getId(),
                product.getPzn(),
                product.getName(),
                product.getManufacturer(),
                product.getAtcCode(),
                round(score),
                round(trigram),
                round(levenshtein),
                round(tokens),
                List.copyOf(explanations)
        );
    }

    private static String searchable(Product product) {
        return NormalizedText.of(String.join(" ",
                nullToEmpty(product.getName()),
                nullToEmpty(product.getActiveIngredient()),
                nullToEmpty(product.getManufacturer()),
                nullToEmpty(product.getStrength())
        ));
    }

    private static double tokenOverlap(String query, String document) {
        Set<String> queryTokens = tokens(query);
        Set<String> documentTokens = tokens(document);
        if (queryTokens.isEmpty() || documentTokens.isEmpty()) {
            return 0.0;
        }
        long hits = queryTokens.stream().filter(documentTokens::contains).count();
        return (double) hits / queryTokens.size();
    }

    private static Set<String> tokens(String text) {
        Set<String> values = new HashSet<>();
        for (String part : text.split(" ")) {
            if (part.length() >= 2) {
                values.add(part);
            }
        }
        return values;
    }

    private static String firstSignificantToken(String normalizedQuery) {
        for (String token : tokens(normalizedQuery)) {
            if (token.length() >= 5) {
                return token;
            }
        }
        return null;
    }

    private static String pct(double value) {
        return Math.round(value * 100) + " %";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.toUpperCase(Locale.ROOT);
    }

    private static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
