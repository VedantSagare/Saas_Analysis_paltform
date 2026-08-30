package com.example.saasanalytics.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantSchema = TenantContext.getTenantSchema();
        return tenantSchema != null ? tenantSchema : "public";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
