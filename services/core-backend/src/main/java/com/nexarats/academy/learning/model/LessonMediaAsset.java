package com.nexarats.academy.learning.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lesson_media_assets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lesson_id", "asset_type"})
})
public class LessonMediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "asset_type", nullable = false, length = 32)
    private String assetType = "VIDEO_STREAM";

    @Column(name = "storage_provider", nullable = false, length = 32)
    private String storageProvider = "INTERNAL_STREAMING";

    @Column(name = "storage_object_key", nullable = false, length = 512)
    private String storageObjectKey;

    @Column(name = "media_url", length = 1024)
    private String mediaUrl;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds = 0;

    @Column(name = "content_version", nullable = false, length = 32)
    private String contentVersion = "1.0.0";

    @Column(name = "status", nullable = false, length = 32)
    private String status = "READY";

    @Column(name = "access_classification", nullable = false, length = 32)
    private String accessClassification = "ENROLLED_ONLY";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public LessonMediaAsset() {
    }

    public LessonMediaAsset(UUID lessonId, String assetType, String storageObjectKey, int durationSeconds) {
        this.lessonId = lessonId;
        this.assetType = assetType;
        this.storageObjectKey = storageObjectKey;
        this.durationSeconds = durationSeconds;
        this.status = "READY";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public void setLessonId(UUID lessonId) {
        this.lessonId = lessonId;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        this.storageProvider = storageProvider;
    }

    public String getStorageObjectKey() {
        return storageObjectKey;
    }

    public void setStorageObjectKey(String storageObjectKey) {
        this.storageObjectKey = storageObjectKey;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = Math.max(0, durationSeconds);
    }

    public String getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(String contentVersion) {
        this.contentVersion = contentVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessClassification() {
        return accessClassification;
    }

    public void setAccessClassification(String accessClassification) {
        this.accessClassification = accessClassification;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
