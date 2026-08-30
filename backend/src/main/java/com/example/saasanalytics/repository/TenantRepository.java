package com.example.saasanalytics.repository;

import com.example.saasanalytics.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByDomain(String domain);
    Optional<Tenant> findBySchemaName(String schemaName);
    boolean existsBySlug(String slug);
    boolean existsByDomain(String domain);
}
