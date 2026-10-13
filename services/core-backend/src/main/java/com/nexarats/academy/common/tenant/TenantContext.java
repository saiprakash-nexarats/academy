package com.nexarats.academy.common.tenant;

import java.util.Objects;
import java.util.UUID;

/**
 * Thread-local context holder for the active tenant.
 *
 * TENANT SECURITY RULE:
 * A client-provided tenant header or subdomain must never be blindly trusted.
 * Future tenant context must come exclusively from authenticated and authorized
 * server-side resolution (e.g. validated JWT claims or verified enterprise domain binding).
 *
 * Database RLS provides defence-in-depth, but is never a replacement for
 * rigorous application authorization and tenant-scoped queries.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Prevent instantiation
    }

    /**
     * Sets the validated tenant ID for the current execution thread.
     *
     * @param tenantId The validated UUID of the tenant.
     */
    public static void setTenantId(UUID tenantId) {
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Retrieves the current tenant ID, or null if no tenant is bound.
     *
     * @return current tenant UUID or null
     */
    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clears the tenant context for the current thread to prevent thread-pool leakage.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
