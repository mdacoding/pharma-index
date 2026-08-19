package de.pharmaindex.ui;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Long id,
        String pzn,
        String name,
        String manufacturer,
        String activeIngredient,
        String atcCode,
        String atcGroup,
        String strength,
        String form,
        String packageSize,
        BigDecimal pharmacyPrice,
        boolean prescriptionRequired,
        String status
) {
}
