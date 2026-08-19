package de.pharmaindex.ops;

import de.pharmaindex.b2b.repo.ImportJobRepository;
import de.pharmaindex.catalog.atc.AtcClassifier;
import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductStatus;
import de.pharmaindex.catalog.repo.ProductRepository;
import de.pharmaindex.matching.ProductMatcher;
import de.pharmaindex.ops.api.dto.DashboardResponse;
import de.pharmaindex.quality.domain.FindingSeverity;
import de.pharmaindex.quality.domain.QualityFinding;
import de.pharmaindex.quality.repo.QualityFindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final QualityFindingRepository findingRepository;
    private final ImportJobRepository importJobRepository;
    private final ProductMatcher productMatcher;

    public DashboardService(
            ProductRepository productRepository,
            QualityFindingRepository findingRepository,
            ImportJobRepository importJobRepository,
            ProductMatcher productMatcher
    ) {
        this.productRepository = productRepository;
        this.findingRepository = findingRepository;
        this.importJobRepository = importJobRepository;
        this.productMatcher = productMatcher;
    }

    @Transactional(readOnly = true)
    public DashboardResponse snapshot() {
        List<Product> products = productRepository.findAll();
        List<QualityFinding> open = findingRepository.findByResolvedFalseOrderBySeverityAscDetectedAtDesc();
        Map<FindingSeverity, Long> bySeverity = open.stream()
                .collect(Collectors.groupingBy(QualityFinding::getSeverity, Collectors.counting()));
        Map<String, Long> byAtc = products.stream()
                .collect(Collectors.groupingBy(product -> AtcClassifier.chapterName(product.getAtcCode()), Collectors.counting()));
        return new DashboardResponse(
                products.size(),
                products.stream().filter(product -> product.getStatus() == ProductStatus.ACTIVE).count(),
                open.size(),
                bySeverity.getOrDefault(FindingSeverity.ERROR, 0L),
                bySeverity.getOrDefault(FindingSeverity.WARNING, 0L),
                bySeverity.getOrDefault(FindingSeverity.INFO, 0L),
                importJobRepository.count(),
                productMatcher.indexSize(),
                byAtc.entrySet().stream()
                        .map(entry -> new DashboardResponse.NamedCount(entry.getKey(), entry.getValue()))
                        .sorted(Comparator.comparingLong(DashboardResponse.NamedCount::count).reversed())
                        .toList(),
                bySeverity.entrySet().stream()
                        .map(entry -> new DashboardResponse.NamedCount(entry.getKey().name(), entry.getValue()))
                        .sorted(Comparator.comparing(DashboardResponse.NamedCount::name))
                        .toList()
        );
    }
}
