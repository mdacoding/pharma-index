CREATE TABLE product_revisions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    pzn VARCHAR(8) NOT NULL,
    name VARCHAR(255) NOT NULL,
    atc_code VARCHAR(16),
    pharmacy_price DECIMAL(10, 2),
    status VARCHAR(20) NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_revision_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_revisions_product ON product_revisions (product_id, changed_at);
