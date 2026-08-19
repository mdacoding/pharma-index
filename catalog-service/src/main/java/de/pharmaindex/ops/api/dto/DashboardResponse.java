package de.pharmaindex.ops.api.dto;

import java.util.List;

public record DashboardResponse(
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
