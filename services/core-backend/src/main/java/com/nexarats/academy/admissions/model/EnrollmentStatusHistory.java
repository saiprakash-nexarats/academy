package com.nexarats.academy.admissions.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollment_status_history")
public class EnrollmentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private EnrollmentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    private EnrollmentStatus toStatus;

    @Column(name = "changed_by_user_id")
    private UUID changedByUserId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public EnrollmentStatusHistory() {
    }

    public EnrollmentStatusHistory(UUID enrollmentId, EnrollmentStatus fromStatus, EnrollmentStatus toStatus, UUID changedByUserId, String reason) {
        this.enrollmentId = enrollmentId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedByUserId = changedByUserId;
        this.reason = reason;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public EnrollmentStatus getFromStatus() {
        return fromStatus;
    }

    public EnrollmentStatus getToStatus() {
        return toStatus;
    }

    public UUID getChangedByUserId() {
        return changedByUserId;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
