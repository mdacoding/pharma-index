package de.pharmaindex.ui;

import java.util.List;

public record DashboardDto(
        long productCount,
        long activeCount,
        long openFindings,
        long errorFindings,
        long warningFindings,
        long infoFindings,
        long importJobs,
        int matchingIndexSize,
        List<NamedCount> byAtcChapter,
        List<NamedCount> bySeverity
) {
    public record NamedCount(String name, long count) {
    }
}
