package de.pharmaindex.catalog.api.dto;

import de.pharmaindex.catalog.atc.AtcClassifier;
import de.pharmaindex.catalog.domain.DosageForm;
import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String pzn,
        String name,
        String manufacturer,
        String activeIngredient,
        String atcCode,
        String atcGroup,
        String strength,
        DosageForm form,
        String packageSize,
        BigDecimal pharmacyPrice,
        boolean prescriptionRequired,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getPzn(),
                product.getName(),
                product.getManufacturer(),
                product.getActiveIngredient(),
                product.getAtcCode(),
                AtcClassifier.chapterName(product.getAtcCode()),
                product.getStrength(),
                product.getForm(),
                product.getPackageSize(),
                product.getPharmacyPrice(),
                product.isPrescriptionRequired(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
