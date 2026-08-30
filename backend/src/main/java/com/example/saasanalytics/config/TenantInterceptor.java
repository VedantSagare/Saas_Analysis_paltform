package com.example.saasanalytics.config;

import com.example.saasanalytics.domain.Tenant;
import com.example.saasanalytics.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private final TenantService tenantService;

    public TenantInterceptor(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantHeader = request.getHeader("X-Tenant-ID");
        String tenantHost = request.getServerName();

        try {
            Tenant tenant = resolveTenant(tenantHeader, tenantHost);
            TenantContext.setTenant(tenant.getId(), tenant.getSchemaName());
            return true;
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            try {
                response.getWriter().write("{\"error\":\"\" + ex.getMessage() + \"\"}");
            } catch (Exception ignored) {
                // no-op
            }
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private Tenant resolveTenant(String tenantHeader, String tenantHost) {
        if (tenantHeader != null && !tenantHeader.isBlank()) {
            return tenantService.resolveTenant(tenantHeader);
        }

        if (tenantHost != null && !tenantHost.equals("localhost") && !tenantHost.equals("127.0.0.1")) {
            String hostName = tenantHost.split("\\.")[0];
            if (!hostName.isBlank() && !"www".equalsIgnoreCase(hostName)) {
                return tenantService.findBySlug(hostName)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown tenant subdomain: " + hostName));
            }
        }

        throw new IllegalArgumentException("Missing tenant identifier. Send X-Tenant-ID header or use a tenant subdomain.");
    }
}
