package de.pharmaindex.b2b.api.dto;

import de.pharmaindex.b2b.domain.ImportJob;
import de.pharmaindex.b2b.domain.ImportStatus;

import java.time.Instant;

public record ImportJobResponse(
        Long id,
        String partner,
        String filename,
        ImportStatus status,
        int recordsOk,
        int recordsError,
        String errorSummary,
        Instant startedAt,
        Instant finishedAt
) {
    public static ImportJobResponse from(ImportJob job) {
        return from(job, job.getPartner().getName());
    }

    public static ImportJobResponse from(ImportJob job, String partnerName) {
        return new ImportJobResponse(
                job.getId(),
                partnerName,
                job.getFilename(),
                job.getStatus(),
                job.getRecordsOk(),
                job.getRecordsError(),
                job.getErrorSummary(),
                job.getStartedAt(),
                job.getFinishedAt()
        );
    }
}
