package com.nexarats.academy.identity.service;

import com.nexarats.academy.identity.model.AuthAuditEvent;
import com.nexarats.academy.identity.repository.AuthAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuthAuditEventRepository auditRepository;

    public AuditService(AuthAuditEventRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public void recordEvent(
            String eventType,
            UUID userId,
            String email,
            UUID tenantId,
            String ipAddress,
            String userAgent,
            String status,
            String failureReason
    ) {
        try {
            AuthAuditEvent event = new AuthAuditEvent(
                    eventType,
                    userId,
                    email,
                    tenantId,
                    ipAddress,
                    userAgent,
                    status,
                    failureReason
            );
            auditRepository.save(event);
            log.info("AUTH_AUDIT: type={} status={} user={} tenant={} ip={} reason={}",
                    eventType, status, email != null ? email : userId, tenantId, ipAddress, failureReason);
        } catch (Exception e) {
            log.error("Failed to write audit event: {}", e.getMessage());
        }
    }
}
