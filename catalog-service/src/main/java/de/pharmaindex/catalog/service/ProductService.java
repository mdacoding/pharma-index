package de.pharmaindex.catalog.service;

import de.pharmaindex.catalog.api.dto.ProductRequest;
import de.pharmaindex.catalog.api.dto.ProductResponse;
import de.pharmaindex.catalog.api.dto.ProductRevisionResponse;
import de.pharmaindex.catalog.domain.ChangeType;
import de.pharmaindex.catalog.domain.Product;
import de.pharmaindex.catalog.domain.ProductStatus;
import de.pharmaindex.catalog.repo.ProductRepository;
import de.pharmaindex.matching.ProductMatcher;
import de.pharmaindex.pzn.PznChecksum;
import de.pharmaindex.quality.QualityEngine;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMatcher productMatcher;
    private final QualityEngine qualityEngine;
    private final ProductRevisionService revisionService;

    public ProductService(
            ProductRepository productRepository,
            ProductMatcher productMatcher,
            QualityEngine qualityEngine,
            ProductRevisionService revisionService
    ) {
        this.productRepository = productRepository;
        this.productMatcher = productMatcher;
        this.qualityEngine = qualityEngine;
        this.revisionService = revisionService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String q, String atc, ProductStatus status, Pageable pageable) {
        String query = blankToNull(q);
        String atcPrefix = blankToNull(atc);
        return productRepository.search(query, atcPrefix, status, pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "productsByPzn", key = "#pzn")
    public ProductResponse getByPzn(String pzn) {
        return ProductResponse.from(requireByPzn(pzn));
    }

    @Transactional(readOnly = true)
    public List<ProductRevisionResponse> revisions(String pzn) {
        return revisionService.history(requireByPzn(pzn));
    }

    @Transactional
    @CacheEvict(cacheNames = "productsByPzn", key = "#request.pzn()")
    public ProductResponse create(ProductRequest request) {
        String pzn = PznChecksum.normalize(request.pzn());
        if (productRepository.existsByPzn(pzn)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PZN " + pzn + " existiert bereits");
        }
        Product product = new Product();
        apply(product, request, pzn);
        Product saved = productRepository.save(product);
        afterWrite(saved, ChangeType.CREATED);
        return ProductResponse.from(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "productsByPzn", key = "#pzn")
    public ProductResponse update(String pzn, ProductRequest request) {
        Product product = requireByPzn(pzn);
        apply(product, request, PznChecksum.normalize(request.pzn()));
        Product saved = productRepository.save(product);
        afterWrite(saved, ChangeType.UPDATED);
        return ProductResponse.from(saved);
    }

    @Transactional
    @CacheEvict(cacheNames = "productsByPzn", key = "#request.pzn()")
    public ProductResponse upsert(ProductRequest request) {
        String pzn = PznChecksum.normalize(request.pzn());
        Product existing = productRepository.findByPzn(pzn).orElse(null);
        ChangeType changeType = existing == null ? ChangeType.CREATED : ChangeType.UPDATED;
        Product product = existing == null ? new Product() : existing;
        apply(product, request, pzn);
        Product saved = productRepository.save(product);
        afterWrite(saved, changeType);
        return ProductResponse.from(saved);
    }

    Product requireByPzn(String pzn) {
        return productRepository.findByPzn(PznChecksum.normalize(pzn))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kein Präparat zur PZN " + pzn));
    }

    private void afterWrite(Product saved, ChangeType changeType) {
        productMatcher.index(saved);
        revisionService.record(saved, changeType);
        qualityEngine.scan(saved);
    }

    private static void apply(Product product, ProductRequest request, String pzn) {
        product.setPzn(pzn);
        product.setName(request.name().trim());
        product.setManufacturer(request.manufacturer().trim());
        product.setActiveIngredient(blankToNull(request.activeIngredient()));
        product.setAtcCode(blankToNull(request.atcCode()) == null ? null : request.atcCode().toUpperCase());
        product.setStrength(blankToNull(request.strength()));
        product.setForm(request.form());
        product.setPackageSize(blankToNull(request.packageSize()));
        product.setPharmacyPrice(request.pharmacyPrice());
        product.setPrescriptionRequired(request.prescriptionRequired());
        product.setStatus(request.status() == null ? ProductStatus.ACTIVE : request.status());
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
