package de.pharmaindex.catalog.repo;

import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRevisionRepository extends JpaRepository<ProductRevision, Long> {

    List<ProductRevision> findByProductOrderByChangedAtDesc(Product product);
}
