package de.pharmaindex.b2b.service;

import de.pharmaindex.b2b.api.dto.ImportJobResponse;
import de.pharmaindex.b2b.domain.ImportJob;
import de.pharmaindex.b2b.domain.ImportStatus;
import de.pharmaindex.b2b.domain.Partner;
import de.pharmaindex.b2b.repo.ImportJobRepository;
import de.pharmaindex.catalog.api.dto.ProductRequest;
import de.pharmaindex.catalog.domain.DosageForm;
import de.pharmaindex.catalog.domain.ProductStatus;
import de.pharmaindex.catalog.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class B2bImportService {

    private final ImportJobRepository importJobRepository;
    private final ProductService productService;

    public B2bImportService(ImportJobRepository importJobRepository, ProductService productService) {
        this.importJobRepository = importJobRepository;
        this.productService = productService;
    }

    public ImportJobResponse importCsv(Partner partner, MultipartFile file) {
        String partnerName = partner.getName();
        ImportJob job = new ImportJob();
        job.setPartner(partner);
        job.setFilename(file.getOriginalFilename());
        job.setStatus(ImportStatus.RUNNING);
        importJobRepository.save(job);

        List<String> errors = new ArrayList<>();
        int ok = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leere Importdatei");
            }
            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                row++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    productService.upsert(parseRow(line));
                    ok++;
                } catch (Exception ex) {
                    errors.add("Zeile " + row + ": " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            job.setStatus(ImportStatus.FAILED);
            job.setErrorSummary(ex.getMessage());
            job.setFinishedAt(Instant.now());
            return ImportJobResponse.from(importJobRepository.save(job), partnerName);
        }

        job.setRecordsOk(ok);
        job.setRecordsError(errors.size());
        job.setErrorSummary(String.join(" | ", errors.stream().limit(12).toList()));
        job.setStatus(ImportStatus.COMPLETED);
        job.setFinishedAt(Instant.now());
        return ImportJobResponse.from(importJobRepository.save(job), partnerName);
    }

    @Transactional(readOnly = true)
    public List<ImportJobResponse> list() {
        return importJobRepository.findAllByOrderByStartedAtDesc().stream()
                .map(ImportJobResponse::from)
                .toList();
    }

    static ProductRequest parseRow(String line) {
        String[] cols = line.split(";", -1);
        if (cols.length < 10) {
            throw new IllegalArgumentException("Erwarte 10 Semikolon-Spalten");
        }
        BigDecimal price = cols[8].isBlank() ? null : new BigDecimal(cols[8].replace(",", "."));
        boolean rx = Boolean.parseBoolean(cols[9].trim());
        return new ProductRequest(
                cols[0].trim(),
                cols[1].trim(),
                cols[2].trim(),
                emptyToNull(cols[3]),
                emptyToNull(cols[4]),
                emptyToNull(cols[5]),
                DosageForm.valueOf(cols[6].trim().toUpperCase(Locale.ROOT)),
                emptyToNull(cols[7]),
                price,
                rx,
                ProductStatus.ACTIVE
        );
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
