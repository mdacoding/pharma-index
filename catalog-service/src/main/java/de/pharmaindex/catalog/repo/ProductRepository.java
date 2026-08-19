package de.pharmaindex.catalog.repo;

import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByPzn(String pzn);

    boolean existsByPzn(String pzn);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.activeIngredient) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.pzn LIKE CONCAT('%', :q, '%'))
              AND (:atc IS NULL OR p.atcCode LIKE CONCAT(:atc, '%'))
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<Product> search(
            @Param("q") String q,
            @Param("atc") String atc,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    List<Product> findByActiveIngredientIgnoreCase(String activeIngredient);
}
