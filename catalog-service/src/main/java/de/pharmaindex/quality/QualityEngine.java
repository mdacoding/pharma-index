package de.pharmaindex.quality;

import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.config.PharmaIndexProperties;
import de.pharmaindex.matching.MatchCandidate;
import de.pharmaindex.matching.ProductMatcher;
import de.pharmaindex.pzn.PznChecksum;
import de.pharmaindex.quality.domain.FindingSeverity;
import de.pharmaindex.quality.domain.FindingType;
import de.pharmaindex.quality.domain.QualityFinding;
import de.pharmaindex.quality.repo.QualityFindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class QualityEngine {

    private static final Pattern ATC = Pattern.compile("^[A-Z]\\d{2}[A-Z]{0,2}\\d{0,2}$");
    private static final Pattern STRENGTH = Pattern.compile("(?i)^\\d+([.,]\\d+)?\\s*(mg|g|ml|µg|mcg|ie|%)\\b.*");

    private final QualityFindingRepository findingRepository;
    private final ProductMatcher productMatcher;
    private final PharmaIndexProperties properties;

    public QualityEngine(
            QualityFindingRepository findingRepository,
            ProductMatcher productMatcher,
            PharmaIndexProperties properties
    ) {
        this.findingRepository = findingRepository;
        this.productMatcher = productMatcher;
        this.properties = properties;
    }

    @Transactional
    public List<QualityFinding> scan(Product product) {
        findingRepository.deleteByProduct(product);
        List<QualityFinding> findings = evaluate(product);
        return findingRepository.saveAll(findings);
    }

    public List<QualityFinding> evaluate(Product product) {
        List<QualityFinding> findings = new ArrayList<>();
        if (!PznChecksum.isValid(product.getPzn())) {
            findings.add(QualityFinding.of(
                    product,
                    FindingType.INVALID_PZN,
                    FindingSeverity.ERROR,
                    "PZN " + product.getPzn() + " verletzt die Prüfzifferregel (Gewichte 2–8, Modulo 11)."
            ));
        }
        if (product.getAtcCode() == null || product.getAtcCode().isBlank()) {
            findings.add(QualityFinding.of(
                    product,
                    FindingType.MISSING_ATC,
                    FindingSeverity.WARNING,
                    "ATC-Code fehlt – Zuordnung in Warenwirtschaft und Arztsoftware unsicher."
            ));
        } else if (!ATC.matcher(product.getAtcCode().toUpperCase()).matches()) {
            findings.add(QualityFinding.of(
                    product,
                    FindingType.INVALID_ATC,
                    FindingSeverity.ERROR,
                    "ATC-Code '" + product.getAtcCode() + "' entspricht nicht dem WHO-Format."
            ));
        }
        if (product.getStrength() == null || product.getStrength().isBlank()) {
            findings.add(QualityFinding.of(
                    product,
                    FindingType.MISSING_STRENGTH,
                    FindingSeverity.WARNING,
                    "Stärke/Wirkstoffgehalt fehlt."
            ));
        } else if (!STRENGTH.matcher(product.getStrength().trim()).matches()) {
            findings.add(QualityFinding.of(
                    product,
                    FindingType.MISSING_STRENGTH,
                    FindingSeverity.INFO,
                    "Stärke '" + product.getStrength() + "' weicht vom erwarteten Muster (z. B. 500 mg) ab."
            ));
        }
        if (product.getPharmacyPrice() != null) {
            BigDecimal price = product.getPharmacyPrice();
            if (price.signum() <= 0) {
                findings.add(QualityFinding.of(
                        product,
                        FindingType.PRICE_ANOMALY,
                        FindingSeverity.ERROR,
                        "Apothekenverkaufspreis muss größer 0 sein."
                ));
            } else if (price.doubleValue() >= properties.getQuality().getPriceWarningEur()) {
                findings.add(QualityFinding.of(
                        product,
                        FindingType.PRICE_ANOMALY,
                        FindingSeverity.WARNING,
                        "Ungewöhnlich hoher AVP (" + price + " EUR) – manuelle Prüfung empfohlen."
                ));
            }
        }
        if (product.getName() != null && product.getName().equals(product.getName().toUpperCase()) && product.getName().length() > 8) {
            findings.add(QualityFinding.of(
                    product,
                    FindingType.NAME_QUALITY,
                    FindingSeverity.INFO,
                    "Handelsname ist durchgängig in Großbuchstaben – Canonical-Schreibweise prüfen."
            ));
        }
        if (product.getId() != null) {
            for (MatchCandidate candidate : productMatcher.match(product.getName(), product.getAtcCode(), product.getId())) {
                if (candidate.score() >= 0.82) {
                    findings.add(QualityFinding.of(
                            product,
                            FindingType.DUPLICATE_CANDIDATE,
                            FindingSeverity.WARNING,
                            "Mögliche Dublette: " + candidate.name() + " (PZN " + candidate.pzn()
                                    + ", Score " + candidate.score() + ")."
                    ));
                }
            }
        }
        return findings;
    }
}
