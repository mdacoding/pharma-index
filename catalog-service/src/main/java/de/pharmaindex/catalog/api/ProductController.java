package de.pharmaindex.catalog.api;

import de.pharmaindex.catalog.api.dto.ProductRequest;
import de.pharmaindex.catalog.api.dto.ProductResponse;
import de.pharmaindex.catalog.api.dto.ProductRevisionResponse;
import de.pharmaindex.catalog.domain.ProductStatus;
import de.pharmaindex.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Katalog", description = "Stammdaten für Fertigarzneimittel (PZN, ATC, Packung)")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Produktsuche mit Paginierung")
    public Page<ProductResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String atc,
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return productService.search(q, atc, status, pageable);
    }

    @GetMapping("/{pzn}")
    @Operation(summary = "Produkt per Pharmazentralnummer laden")
    public ProductResponse get(@PathVariable String pzn) {
        return productService.getByPzn(pzn);
    }

    @GetMapping("/{pzn}/revisions")
    @Operation(summary = "Änderungshistorie eines Präparats")
    public List<ProductRevisionResponse> revisions(@PathVariable String pzn) {
        return productService.revisions(pzn);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Neues Präparat anlegen")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{pzn}")
    @Operation(summary = "Stammdaten aktualisieren")
    public ProductResponse update(@PathVariable String pzn, @Valid @RequestBody ProductRequest request) {
        return productService.update(pzn, request);
    }
}
