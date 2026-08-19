package de.pharmaindex.ops.api;

import de.pharmaindex.ops.DashboardService;
import de.pharmaindex.ops.api.dto.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ops")
@Tag(name = "Betrieb", description = "Kennzahlen für QA, Skalierung und Fachredaktion")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "KPI-Snapshot: Katalog, Findings, Matching-Index")
    public DashboardResponse dashboard() {
        return dashboardService.snapshot();
    }
}
