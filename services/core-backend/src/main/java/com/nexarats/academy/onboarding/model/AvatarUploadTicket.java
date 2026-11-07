package com.nexarats.academy.onboarding.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "avatar_upload_tickets")
public class AvatarUploadTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "quarantine_key", nullable = false, unique = true, length = 512)
    private String quarantineKey;

    @Column(name = "approved_key", length = 512)
    private String approvedKey;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 64)
    private String contentType;

    @Column(name = "file_extension", nullable = false, length = 16)
    private String fileExtension;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ISSUED";

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    public AvatarUploadTicket() {
    }

    public AvatarUploadTicket(UUID tenantId, UUID userId, String quarantineKey, String originalFilename,
                              String contentType, String fileExtension, Instant expiresAt) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.quarantineKey = quarantineKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileExtension = fileExtension;
        this.status = "ISSUED";
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isIssued() {
        return "ISSUED".equalsIgnoreCase(status);
    }

    public void confirm(String approvedKey) {
        this.status = "CONFIRMED";
        this.approvedKey = approvedKey;
        this.confirmedAt = Instant.now();
    }

    public void expire() {
        this.status = "EXPIRED";
    }

    public void markQuarantined() {
        this.status = "QUARANTINED";
    }

    public void markInfected() {
        this.status = "INFECTED";
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getQuarantineKey() {
        return quarantineKey;
    }

    public void setQuarantineKey(String quarantineKey) {
        this.quarantineKey = quarantineKey;
    }

    public String getApprovedKey() {
        return approvedKey;
    }

    public void setApprovedKey(String approvedKey) {
        this.approvedKey = approvedKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
