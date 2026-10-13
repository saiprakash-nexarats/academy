package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.TenantMembership;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantMembershipRepository extends JpaRepository<TenantMembership, UUID> {
    Optional<TenantMembership> findByTenantAndUser(Tenant tenant, User user);
    Optional<TenantMembership> findByTenantIdAndUserId(UUID tenantId, UUID userId);
    List<TenantMembership> findByUser(User user);
    List<TenantMembership> findByUserId(UUID userId);
    List<TenantMembership> findByTenantId(UUID tenantId);
    boolean existsByTenantIdAndUserId(UUID tenantId, UUID userId);
}
