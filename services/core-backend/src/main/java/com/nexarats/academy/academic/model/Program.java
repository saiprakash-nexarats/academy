package com.nexarats.academy.academic.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "programs")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ProgramCategory category;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(name = "image_asset_key", nullable = false, length = 100)
    private String imageAssetKey;

    @Column(name = "duration_value", nullable = false)
    private int durationValue;

    @Column(name = "duration_unit", nullable = false, length = 20)
    private String durationUnit = "MONTHS";

    @Column(name = "duration_label", nullable = false, length = 50)
    private String durationLabel;

    @Column(nullable = false, length = 50)
    private String level;

    @Column(length = 50)
    private String badge;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.valueOf(4.80);

    @Column(name = "curriculum_overview", columnDefinition = "TEXT")
    private String curriculumOverview;

    @Column(name = "career_outcomes", columnDefinition = "TEXT")
    private String careerOutcomes;

    @Column(nullable = false, length = 30)
    private String status = "PUBLISHED";

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Program() {
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status != null && this.status.equals("PUBLISHED") && this.publishedAt == null) {
            this.publishedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        if (this.status != null && this.status.equals("PUBLISHED") && this.publishedAt == null) {
            this.publishedAt = Instant.now();
        }
        this.updatedAt = Instant.now();
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

    public ProgramCategory getCategory() {
        return category;
    }

    public void setCategory(ProgramCategory category) {
        this.category = category;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getImageAssetKey() {
        return imageAssetKey;
    }

    public void setImageAssetKey(String imageAssetKey) {
        this.imageAssetKey = imageAssetKey;
    }

    public int getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(int durationValue) {
        this.durationValue = durationValue;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public String getDurationLabel() {
        return durationLabel;
    }

    public void setDurationLabel(String durationLabel) {
        this.durationLabel = durationLabel;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getCurriculumOverview() {
        return curriculumOverview;
    }

    public void setCurriculumOverview(String curriculumOverview) {
        this.curriculumOverview = curriculumOverview;
    }

    public String getCareerOutcomes() {
        return careerOutcomes;
    }

    public void setCareerOutcomes(String careerOutcomes) {
        this.careerOutcomes = careerOutcomes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Program program = (Program) o;
        return Objects.equals(id, program.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
