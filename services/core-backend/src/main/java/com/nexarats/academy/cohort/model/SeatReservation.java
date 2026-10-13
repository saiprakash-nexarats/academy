package com.nexarats.academy.cohort.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seat_reservations")
public class SeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cohort_id", nullable = false)
    private UUID cohortId;

    @Column(name = "applicant_user_id")
    private UUID applicantUserId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "offer_id")
    private UUID offerId;

    @Column(name = "reservation_token", nullable = false, unique = true, length = 128)
    private String reservationToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SeatReservationStatus status = SeatReservationStatus.RESERVED;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public SeatReservation() {}

    public SeatReservation(UUID cohortId, String reservationToken, Instant expiresAt) {
        this.cohortId = cohortId;
        this.reservationToken = reservationToken;
        this.expiresAt = expiresAt;
        this.status = SeatReservationStatus.RESERVED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public void setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
    }

    public UUID getApplicantUserId() {
        return applicantUserId;
    }

    public void setApplicantUserId(UUID applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getOfferId() {
        return offerId;
    }

    public void setOfferId(UUID offerId) {
        this.offerId = offerId;
    }

    public String getReservationToken() {
        return reservationToken;
    }

    public void setReservationToken(String reservationToken) {
        this.reservationToken = reservationToken;
    }

    public SeatReservationStatus getStatus() {
        return status;
    }

    public void setStatus(SeatReservationStatus status) {
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
