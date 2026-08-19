package de.pharmaindex.ui;

public record QualityFindingDto(
        Long id,
        String pzn,
        String productName,
        String type,
        String severity,
        String message
) {
}
