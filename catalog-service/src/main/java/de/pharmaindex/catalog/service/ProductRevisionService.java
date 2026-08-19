package de.pharmaindex.catalog.service;

import de.pharmaindex.catalog.api.dto.ProductRevisionResponse;
import de.pharmaindex.catalog.domain.ChangeType;
import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductRevision;
import de.pharmaindex.catalog.repo.ProductRevisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductRevisionService {

    private final ProductRevisionRepository revisionRepository;

    public ProductRevisionService(ProductRevisionRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }

    public void record(Product product, ChangeType changeType) {
        revisionRepository.save(ProductRevision.snapshot(product, changeType));
    }

    @Transactional(readOnly = true)
    public List<ProductRevisionResponse> history(Product product) {
        return revisionRepository.findByProductOrderByChangedAtDesc(product).stream()
                .map(ProductRevisionResponse::from)
                .toList();
    }
}
