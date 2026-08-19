package de.pharmaindex.quality.repo;

import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.quality.domain.FindingSeverity;
import de.pharmaindex.quality.domain.QualityFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QualityFindingRepository extends JpaRepository<QualityFinding, Long> {

    @Query("SELECT f FROM QualityFinding f JOIN FETCH f.product WHERE f.resolved = false ORDER BY f.severity ASC, f.detectedAt DESC")
    List<QualityFinding> findByResolvedFalseOrderBySeverityAscDetectedAtDesc();

    @Query("SELECT f FROM QualityFinding f JOIN FETCH f.product WHERE f.resolved = false AND f.severity = :severity ORDER BY f.detectedAt DESC")
    List<QualityFinding> findByResolvedFalseAndSeverityOrderByDetectedAtDesc(@Param("severity") FindingSeverity severity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByProduct(Product product);

    long countByResolvedFalse();
}
