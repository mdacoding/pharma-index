package de.pharmaindex.b2b.api;

import de.pharmaindex.b2b.api.dto.ImportJobResponse;
import de.pharmaindex.b2b.domain.Partner;
import de.pharmaindex.b2b.service.B2bImportService;
import de.pharmaindex.security.ApiKeyAuthFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/b2b")
@Tag(name = "B2B", description = "Partner-Schnittstellen für Warenwirtschaft und Datenlieferanten")
public class B2bImportController {

    private final B2bImportService b2bImportService;

    public B2bImportController(B2bImportService b2bImportService) {
        this.b2bImportService = b2bImportService;
    }

    @PostMapping(path = "/imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "CSV-Stammdatenimport (Semikolon, UTF-8)")
    public ImportJobResponse importCsv(HttpServletRequest request, @RequestPart("file") MultipartFile file) {
        Partner partner = (Partner) request.getAttribute(ApiKeyAuthFilter.PARTNER_ATTRIBUTE);
        return b2bImportService.importCsv(partner, file);
    }

    @GetMapping("/imports")
    @Operation(summary = "Import-Jobs des Katalogs")
    public List<ImportJobResponse> jobs() {
        return b2bImportService.list();
    }
}
