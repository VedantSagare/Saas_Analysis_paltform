package com.example.saasanalytics.config;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT_SCHEMA = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenant(Long tenantId, String tenantSchema) {
        CURRENT_TENANT_ID.set(tenantId);
        CURRENT_TENANT_SCHEMA.set(tenantSchema);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT_ID.get();
    }

    public static String getTenantSchema() {
        return CURRENT_TENANT_SCHEMA.get();
    }

    public static void clear() {
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_SCHEMA.remove();
    }
}
