package com.example.saasanalytics.repository;

import com.example.saasanalytics.domain.ApiKey;
import com.example.saasanalytics.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyValueAndActiveTrue(String keyValue);
    Optional<ApiKey> findByKeyValueAndTenant(String keyValue, Tenant tenant);
}
