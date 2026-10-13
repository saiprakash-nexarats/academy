package com.nexarats.academy.admissions.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"learner_user_id", "cohort_id"})
})
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "enrollment_number", nullable = false, unique = true, length = 64)
    private String enrollmentNumber;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "learner_user_id", nullable = false)
    private UUID learnerUserId;

    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "cohort_id", nullable = false)
    private UUID cohortId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "payment_order_id")
    private UUID paymentOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Enrollment() {
    }

    public Enrollment(String enrollmentNumber, UUID learnerUserId, UUID cohortId, EnrollmentStatus status) {
        this.enrollmentNumber = enrollmentNumber;
        this.learnerUserId = learnerUserId;
        this.cohortId = cohortId;
        this.status = status;
        this.enrolledAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        this.enrollmentNumber = enrollmentNumber;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getLearnerUserId() {
        return learnerUserId;
    }

    public void setLearnerUserId(UUID learnerUserId) {
        this.learnerUserId = learnerUserId;
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

    public UUID getCohortId() {
        return cohortId;
    }

    public void setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(UUID paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Instant enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
