package com.example.saasanalytics.service;

import com.example.saasanalytics.config.TenantContext;
import com.example.saasanalytics.domain.Tenant;
import com.example.saasanalytics.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final DataSource dataSource;

    public TenantService(TenantRepository tenantRepository, DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.dataSource = dataSource;
    }

    @Transactional
    public Tenant createTenant(String name, String domain, String slug) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant name is required");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("Tenant domain is required");
        }

        String normalizedSlug = (slug == null || slug.isBlank()) ? name : slug;
        String safeSlug = normalizedSlug.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-");
        String schemaName = "tenant_" + safeSlug;

        if (tenantRepository.existsBySlug(safeSlug)) {
            throw new IllegalArgumentException("Tenant slug already exists");
        }
        if (tenantRepository.existsByDomain(domain)) {
            throw new IllegalArgumentException("Tenant domain already exists");
        }

        createSchemaIfMissing(schemaName);

        Tenant tenant = new Tenant();
        tenant.setName(name.trim());
        tenant.setSlug(safeSlug);
        tenant.setDomain(domain.trim());
        tenant.setSchemaName(schemaName);
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }

    public Optional<Tenant> findBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return tenantRepository.findBySlug(slug.trim().toLowerCase(Locale.ROOT));
    }

    public Optional<Tenant> findByDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return Optional.empty();
        }
        return tenantRepository.findByDomain(domain.trim().toLowerCase(Locale.ROOT));
    }

    public Optional<Tenant> findBySchemaName(String schemaName) {
        return tenantRepository.findBySchemaName(schemaName);
    }

    public Tenant resolveTenant(String identifierOrDomain) {
        if (identifierOrDomain == null || identifierOrDomain.isBlank()) {
            throw new IllegalArgumentException("Tenant identifier is required");
        }

        String normalized = identifierOrDomain.trim().toLowerCase(Locale.ROOT);
        return tenantRepository.findBySlug(normalized)
                .or(() -> tenantRepository.findByDomain(normalized))
                .orElseThrow(() -> new IllegalArgumentException("Unknown tenant: " + normalized));
    }

    public Tenant getCurrentTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant is not available in the current request");
        }
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found for id " + tenantId));
    }

    private void createSchemaIfMissing(String schemaName) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize tenant schema " + schemaName, e);
        }
    }
}
