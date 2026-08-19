package de.pharmaindex.catalog.api.dto;

import de.pharmaindex.catalog.domain.DosageForm;
import de.pharmaindex.catalog.domain.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(min = 7, max = 8) String pzn,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String manufacturer,
        @Size(max = 255) String activeIngredient,
        @Size(max = 16) String atcCode,
        @Size(max = 64) String strength,
        @NotNull DosageForm form,
        @Size(max = 64) String packageSize,
        BigDecimal pharmacyPrice,
        boolean prescriptionRequired,
        ProductStatus status
) {
}
