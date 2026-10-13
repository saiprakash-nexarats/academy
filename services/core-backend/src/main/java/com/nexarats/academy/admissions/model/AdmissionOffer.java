package com.nexarats.academy.admissions.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admission_offers")
public class AdmissionOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "offer_number", nullable = false, unique = true, length = 64)
    private String offerNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "cohort_id", nullable = false)
    private UUID cohortId;

    @Column(name = "tuition_price_id")
    private UUID tuitionPriceId;

    @Column(name = "offered_amount_cents", nullable = false)
    private long offeredAmountCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AdmissionOfferStatus status = AdmissionOfferStatus.ISSUED;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "issued_by_user_id")
    private UUID issuedByUserId;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public AdmissionOffer() {
    }

    public AdmissionOffer(String offerNumber, Application application, UUID cohortId, long offeredAmountCents, String currency, Instant validUntil) {
        this.offerNumber = offerNumber;
        this.application = application;
        this.cohortId = cohortId;
        this.offeredAmountCents = offeredAmountCents;
        this.currency = currency;
        this.validUntil = validUntil;
        this.status = AdmissionOfferStatus.ISSUED;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOfferNumber() {
        return offerNumber;
    }

    public void setOfferNumber(String offerNumber) {
        this.offerNumber = offerNumber;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public void setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
    }

    public UUID getTuitionPriceId() {
        return tuitionPriceId;
    }

    public void setTuitionPriceId(UUID tuitionPriceId) {
        this.tuitionPriceId = tuitionPriceId;
    }

    public long getOfferedAmountCents() {
        return offeredAmountCents;
    }

    public void setOfferedAmountCents(long offeredAmountCents) {
        this.offeredAmountCents = offeredAmountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AdmissionOfferStatus getStatus() {
        return status;
    }

    public void setStatus(AdmissionOfferStatus status) {
        this.status = status;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public UUID getIssuedByUserId() {
        return issuedByUserId;
    }

    public void setIssuedByUserId(UUID issuedByUserId) {
        this.issuedByUserId = issuedByUserId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Instant respondedAt) {
        this.respondedAt = respondedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
