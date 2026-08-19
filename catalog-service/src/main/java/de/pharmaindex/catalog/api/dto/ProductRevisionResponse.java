package de.pharmaindex.catalog.api.dto;

import de.pharmaindex.catalog.domain.ChangeType;
import de.pharmaindex.catalog.domain.ProductRevision;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductRevisionResponse(
        Long id,
        ChangeType changeType,
        String pzn,
        String name,
        String atcCode,
        BigDecimal pharmacyPrice,
        String status,
        Instant changedAt
) {
    public static ProductRevisionResponse from(ProductRevision revision) {
        return new ProductRevisionResponse(
                revision.getId(),
                revision.getChangeType(),
                revision.getPzn(),
                revision.getName(),
                revision.getAtcCode(),
                revision.getPharmacyPrice(),
                revision.getStatus(),
                revision.getChangedAt()
        );
    }
}
