package com.nexarats.academy.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantById(UUID id) {
        return tenantRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }

    public Tenant getOrCreateTenant(String slug, String name) {
        return tenantRepository.findBySlug(slug)
                .orElseGet(() -> tenantRepository.save(new Tenant(slug, name)));
    }

    public Tenant createTenant(String slug, String name) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Tenant with slug already exists: " + slug);
        }
        return tenantRepository.save(new Tenant(slug, name));
    }
}
