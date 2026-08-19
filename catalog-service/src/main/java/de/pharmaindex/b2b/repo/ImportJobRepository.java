package de.pharmaindex.b2b.repo;

import de.pharmaindex.b2b.domain.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    @Query("SELECT j FROM ImportJob j JOIN FETCH j.partner ORDER BY j.startedAt DESC")
    List<ImportJob> findAllByOrderByStartedAtDesc();
}
