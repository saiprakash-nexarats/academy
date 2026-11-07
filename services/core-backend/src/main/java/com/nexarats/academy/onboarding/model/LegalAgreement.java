package com.nexarats.academy.onboarding.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "legal_agreements", uniqueConstraints = {
        @UniqueConstraint(name = "uq_legal_agreements_type_version", columnNames = {"agreement_type", "version"})
})
public class LegalAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 64)
    private LegalAgreementType agreementType;

    @Column(name = "version", nullable = false, length = 32)
    private String version;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "effective_date", nullable = false)
    private Instant effectiveDate = Instant.now();

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LegalAgreement() {
    }

    public LegalAgreement(UUID tenantId, LegalAgreementType agreementType, String version, String title,
                          String content, String contentHash) {
        this.tenantId = tenantId;
        this.agreementType = agreementType;
        this.version = version;
        this.title = title;
        this.content = content;
        this.contentHash = contentHash;
        this.effectiveDate = Instant.now();
        this.isActive = true;
        this.createdAt = Instant.now();
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

    public LegalAgreementType getAgreementType() {
        return agreementType;
    }

    public void setAgreementType(LegalAgreementType agreementType) {
        this.agreementType = agreementType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Instant getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Instant effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
