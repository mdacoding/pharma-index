package de.pharmaindex.quality;

import de.pharmaindex.catalog.domain.DosageForm;
import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductStatus;
import de.pharmaindex.config.PharmaIndexProperties;
import de.pharmaindex.matching.ProductMatcher;
import de.pharmaindex.pzn.PznChecksum;
import de.pharmaindex.quality.domain.FindingType;
import de.pharmaindex.quality.repo.QualityFindingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QualityEngineTest {

    @Mock
    private QualityFindingRepository findingRepository;
    @Mock
    private ProductMatcher productMatcher;

    private QualityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new QualityEngine(findingRepository, productMatcher, new PharmaIndexProperties());
    }

    @Test
    void flagsInvalidPznMissingAtcAndPriceSpike() {
        Product product = new Product();
        product.setPzn("11111111");
        product.setName("TESTPRODUKT ALPHA");
        product.setManufacturer("Demo");
        product.setForm(DosageForm.TABLETTE);
        product.setStatus(ProductStatus.DRAFT);
        product.setPharmacyPrice(new BigDecimal("400.00"));

        var types = engine.evaluate(product).stream().map(finding -> finding.getType()).toList();

        assertThat(types).contains(FindingType.INVALID_PZN, FindingType.MISSING_ATC, FindingType.PRICE_ANOMALY);
    }

    @Test
    void acceptsValidFinishedProduct() {
        Product product = new Product();
        product.setPzn(PznChecksum.withCheckDigit("9900001"));
        product.setName("Aspirin 500 mg Tabletten");
        product.setManufacturer("Bayer Vital");
        product.setAtcCode("N02BA01");
        product.setStrength("500 mg");
        product.setForm(DosageForm.TABLETTE);
        product.setPharmacyPrice(new BigDecimal("3.49"));
        product.setStatus(ProductStatus.ACTIVE);

        assertThat(engine.evaluate(product)).isEmpty();
    }
}
