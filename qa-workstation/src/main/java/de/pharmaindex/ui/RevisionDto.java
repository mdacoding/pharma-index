package de.pharmaindex.ui;

import java.time.Instant;

public record RevisionDto(
        Long id,
        String changeType,
        String pzn,
        String name,
        String atcCode,
        String status,
        Instant changedAt
) {
}
