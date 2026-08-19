package de.pharmaindex.quality.api.dto;

import de.pharmaindex.quality.domain.FindingSeverity;
import de.pharmaindex.quality.domain.FindingType;
import de.pharmaindex.quality.domain.QualityFinding;

import java.time.Instant;

public record QualityFindingResponse(
        Long id,
        String pzn,
        String productName,
        FindingType type,
        FindingSeverity severity,
        String message,
        Instant detectedAt
) {
    public static QualityFindingResponse from(QualityFinding finding) {
        return new QualityFindingResponse(
                finding.getId(),
                finding.getProduct().getPzn(),
                finding.getProduct().getName(),
                finding.getType(),
                finding.getSeverity(),
                finding.getMessage(),
                finding.getDetectedAt()
        );
    }
}
