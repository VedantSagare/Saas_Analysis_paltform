package com.example.saasanalytics.controller;

import com.example.saasanalytics.domain.Tenant;
import com.example.saasanalytics.service.TenantService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<?> onboard(@RequestBody TenantOnboardingRequest request) {
        try {
            Tenant tenant = tenantService.createTenant(request.name(), request.domain(), request.slug());
            return ResponseEntity.ok(new TenantResponse(tenant.getId(), tenant.getName(), tenant.getSlug(), tenant.getDomain(), tenant.getSchemaName()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Tenant onboarding failed: " + ex.getMessage());
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> current() {
        try {
            Tenant tenant = tenantService.getCurrentTenant();
            return ResponseEntity.ok(new TenantResponse(tenant.getId(), tenant.getName(), tenant.getSlug(), tenant.getDomain(), tenant.getSchemaName()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    public record TenantOnboardingRequest(
            @NotBlank String name,
            @NotBlank String domain,
            String slug
    ) {}

    public record TenantResponse(Long id, String name, String slug, String domain, String schemaName) {}
}
