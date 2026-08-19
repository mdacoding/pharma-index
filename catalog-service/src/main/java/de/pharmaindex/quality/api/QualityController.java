package de.pharmaindex.quality.api;

import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.repo.ProductRepository;
import de.pharmaindex.quality.QualityEngine;
import de.pharmaindex.quality.api.dto.QualityFindingResponse;
import de.pharmaindex.quality.domain.FindingSeverity;
import de.pharmaindex.quality.domain.QualityFinding;
import de.pharmaindex.quality.repo.QualityFindingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/qa")
@Tag(name = "Qualitätssicherung", description = "Regelwerk und Findings zu Stammdaten")
public class QualityController {

    private final QualityFindingRepository findingRepository;
    private final QualityEngine qualityEngine;
    private final ProductRepository productRepository;

    public QualityController(
            QualityFindingRepository findingRepository,
            QualityEngine qualityEngine,
            ProductRepository productRepository
    ) {
        this.findingRepository = findingRepository;
        this.qualityEngine = qualityEngine;
        this.productRepository = productRepository;
    }

    @GetMapping("/findings")
    @Transactional(readOnly = true)
    @Operation(summary = "Offene QA-Findings")
    public List<QualityFindingResponse> findings(@RequestParam(required = false) FindingSeverity severity) {
        var open = severity == null
                ? findingRepository.findByResolvedFalseOrderBySeverityAscDetectedAtDesc()
                : findingRepository.findByResolvedFalseAndSeverityOrderByDetectedAtDesc(severity);
        return open.stream().map(QualityFindingResponse::from).toList();
    }

    @GetMapping(value = "/findings.csv", produces = "text/csv")
    @Transactional(readOnly = true)
    @Operation(summary = "Offene Findings als CSV für Fachredaktion")
    public ResponseEntity<byte[]> findingsCsv() {
        StringBuilder csv = new StringBuilder("severity;type;pzn;product;message\n");
        findingRepository.findByResolvedFalseOrderBySeverityAscDetectedAtDesc()
                .forEach(finding -> csv.append(finding.getSeverity()).append(';')
                        .append(finding.getType()).append(';')
                        .append(finding.getProduct().getPzn()).append(';')
                        .append(escape(finding.getProduct().getName())).append(';')
                        .append(escape(finding.getMessage())).append('\n'));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=qa-findings.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/summary")
    @Operation(summary = "Anzahl offener Findings")
    public Map<String, Long> summary() {
        return Map.of("openFindings", findingRepository.countByResolvedFalse());
    }

    @PostMapping("/findings/{id}/resolve")
    @Transactional
    @Operation(summary = "Finding als geprüft markieren")
    public QualityFindingResponse resolve(@PathVariable Long id) {
        QualityFinding finding = findingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Finding " + id));
        finding.setResolved(true);
        return QualityFindingResponse.from(findingRepository.save(finding));
    }

    @PostMapping("/scan")
    @Operation(summary = "Vollständigen Qualitätslauf über den Katalog starten")
    public Map<String, Integer> scanAll() {
        int findings = 0;
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            findings += qualityEngine.scan(product).size();
        }
        return Map.of("products", products.size(), "findings", findings);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(';', ',').replace('\n', ' ');
    }
}
