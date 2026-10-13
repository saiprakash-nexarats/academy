package com.nexarats.academy.finance.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tuition_prices")
public class TuitionPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "display_price", nullable = false, length = 64)
    private String displayPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 32)
    private TuitionTier tier = TuitionTier.STANDARD;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public TuitionPrice() {
    }

    public TuitionPrice(String currency, long amountCents, String displayPrice, TuitionTier tier) {
        this.currency = currency;
        this.amountCents = amountCents;
        this.displayPrice = displayPrice;
        this.tier = tier;
        this.isActive = true;
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

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getProgramId() {
        return programId;
    }

    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public String getDisplayPrice() {
        return displayPrice;
    }

    public void setDisplayPrice(String displayPrice) {
        this.displayPrice = displayPrice;
    }

    public TuitionTier getTier() {
        return tier;
    }

    public void setTier(TuitionTier tier) {
        this.tier = tier;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
