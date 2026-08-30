package com.example.saasanalytics.service;

import com.example.saasanalytics.config.TenantContext;
import com.example.saasanalytics.domain.ApiKey;
import com.example.saasanalytics.domain.Tenant;
import com.example.saasanalytics.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public Optional<ApiKey> validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return apiKeyRepository.findByKeyValueAndActiveTrue(apiKey)
                    .filter(k -> k.getTenant() != null && k.getTenant().getId().equals(tenantId));
        }

        return apiKeyRepository.findByKeyValueAndActiveTrue(apiKey);
    }

    public ApiKey createApiKey(String name, Tenant tenant, String keyValue) {
        ApiKey key = new ApiKey();
        key.setName(name);
        key.setTenant(tenant);
        key.setKeyValue(keyValue);
        key.setActive(true);
        return apiKeyRepository.save(key);
    }
}
