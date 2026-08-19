CREATE TABLE partners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pzn VARCHAR(8) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    active_ingredient VARCHAR(255),
    atc_code VARCHAR(16),
    strength VARCHAR(64),
    form VARCHAR(32) NOT NULL,
    package_size VARCHAR(64),
    pharmacy_price DECIMAL(10, 2),
    prescription_required BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_atc ON products (atc_code);
CREATE INDEX idx_products_ingredient ON products (active_ingredient);
CREATE INDEX idx_products_name ON products (name);
CREATE INDEX idx_products_status ON products (status);

CREATE TABLE quality_findings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    finding_type VARCHAR(40) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message VARCHAR(500) NOT NULL,
    detected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_finding_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_findings_open ON quality_findings (resolved, severity);

CREATE TABLE import_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_id BIGINT NOT NULL,
    filename VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    records_ok INT NOT NULL DEFAULT 0,
    records_error INT NOT NULL DEFAULT 0,
    error_summary VARCHAR(2000),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMP,
    CONSTRAINT fk_import_partner FOREIGN KEY (partner_id) REFERENCES partners (id)
);
