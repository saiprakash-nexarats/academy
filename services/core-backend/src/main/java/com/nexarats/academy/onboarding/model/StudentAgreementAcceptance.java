package com.nexarats.academy.onboarding.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_agreement_acceptances", uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_agreement_acceptance", columnNames = {"tenant_id", "student_user_id", "agreement_id", "agreement_version"})
})
public class StudentAgreementAcceptance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "student_user_id", nullable = false)
    private UUID studentUserId;

    @Column(name = "agreement_id", nullable = false)
    private UUID agreementId;

    @Column(name = "agreement_version", nullable = false, length = 32)
    private String agreementVersion;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "accepted_at", nullable = false, updatable = false)
    private Instant acceptedAt = Instant.now();

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "acceptance_method", nullable = false, length = 32)
    private String acceptanceMethod = "CLICKWRAP_CHECKBOX";

    @Column(name = "is_legacy_unverified", nullable = false)
    private boolean legacyUnverified = false;

    public StudentAgreementAcceptance() {
    }

    public StudentAgreementAcceptance(UUID tenantId, UUID studentUserId, UUID agreementId,
                                      String agreementVersion, String contentHash,
                                      String ipAddress, String userAgent) {
        this.tenantId = tenantId;
        this.studentUserId = studentUserId;
        this.agreementId = agreementId;
        this.agreementVersion = agreementVersion;
        this.contentHash = contentHash;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.acceptanceMethod = "CLICKWRAP_CHECKBOX";
        this.acceptedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(UUID studentUserId) {
        this.studentUserId = studentUserId;
    }

    public UUID getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(UUID agreementId) {
        this.agreementId = agreementId;
    }

    public String getAgreementVersion() {
        return agreementVersion;
    }

    public void setAgreementVersion(String agreementVersion) {
        this.agreementVersion = agreementVersion;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAcceptanceMethod() {
        return acceptanceMethod;
    }

    public void setAcceptanceMethod(String acceptanceMethod) {
        this.acceptanceMethod = acceptanceMethod;
    }

    public boolean isLegacyUnverified() {
        return legacyUnverified;
    }

    public void setLegacyUnverified(boolean legacyUnverified) {
        this.legacyUnverified = legacyUnverified;
    }
}
