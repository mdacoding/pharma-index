package de.pharmaindex.b2b.repo;

import de.pharmaindex.b2b.domain.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Optional<Partner> findByApiKeyAndActiveTrue(String apiKey);
}
