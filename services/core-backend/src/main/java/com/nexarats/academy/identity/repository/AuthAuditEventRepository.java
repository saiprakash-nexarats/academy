package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.AuthAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthAuditEventRepository extends JpaRepository<AuthAuditEvent, UUID> {
    List<AuthAuditEvent> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<AuthAuditEvent> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
