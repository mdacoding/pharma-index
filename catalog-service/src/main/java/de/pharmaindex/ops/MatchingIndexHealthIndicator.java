package de.pharmaindex.ops;

import de.pharmaindex.matching.ProductMatcher;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MatchingIndexHealthIndicator implements HealthIndicator {

    private final ProductMatcher productMatcher;

    public MatchingIndexHealthIndicator(ProductMatcher productMatcher) {
        this.productMatcher = productMatcher;
    }

    @Override
    public Health health() {
        int size = productMatcher.indexSize();
        return Health.up()
                .withDetail("indexedProducts", size)
                .withDetail("index", "trigram-inverted")
                .build();
    }
}
