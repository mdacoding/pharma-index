package de.pharmaindex.quality.domain;

import de.pharmaindex.catalog.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "quality_findings")
public class QualityFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "finding_type", nullable = false, length = 40)
    private FindingType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FindingSeverity severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(nullable = false)
    private boolean resolved;

    public static QualityFinding of(Product product, FindingType type, FindingSeverity severity, String message) {
        QualityFinding finding = new QualityFinding();
        finding.product = product;
        finding.type = type;
        finding.severity = severity;
        finding.message = message;
        finding.resolved = false;
        return finding;
    }

    @PrePersist
    void onCreate() {
        if (detectedAt == null) {
            detectedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public FindingType getType() {
        return type;
    }

    public FindingSeverity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
