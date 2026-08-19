package de.pharmaindex.catalog.domain;

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

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_revisions")
public class ProductRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, length = 8)
    private String pzn;

    @Column(nullable = false)
    private String name;

    @Column(name = "atc_code", length = 16)
    private String atcCode;

    @Column(name = "pharmacy_price", precision = 10, scale = 2)
    private BigDecimal pharmacyPrice;

    @Column(nullable = false, length = 20)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public static ProductRevision snapshot(Product product, ChangeType changeType) {
        ProductRevision revision = new ProductRevision();
        revision.product = product;
        revision.pzn = product.getPzn();
        revision.name = product.getName();
        revision.atcCode = product.getAtcCode();
        revision.pharmacyPrice = product.getPharmacyPrice();
        revision.status = product.getStatus().name();
        revision.changeType = changeType;
        return revision;
    }

    @PrePersist
    void onCreate() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getPzn() {
        return pzn;
    }

    public String getName() {
        return name;
    }

    public String getAtcCode() {
        return atcCode;
    }

    public BigDecimal getPharmacyPrice() {
        return pharmacyPrice;
    }

    public String getStatus() {
        return status;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
