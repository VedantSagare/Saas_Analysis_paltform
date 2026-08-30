package com.example.saasanalytics.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @Test
    void shouldStoreAndClearTenantState() {
        TenantContext.setTenant(7L, "tenant_alpha");

        assertEquals(7L, TenantContext.getTenantId());
        assertEquals("tenant_alpha", TenantContext.getTenantSchema());

        TenantContext.clear();

        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getTenantSchema());
    }
}
