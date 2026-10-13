package com.nexarats.academy.admissions.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "application_number", nullable = false, unique = true, length = 64)
    private String applicationNumber;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "applicant_user_id")
    private UUID applicantUserId;

    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "cohort_id")
    private UUID cohortId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", nullable = false, length = 64)
    private String phone;

    @Column(name = "experience_level", nullable = false, length = 64)
    private String experienceLevel = "beginner";

    @Column(name = "statement_of_purpose", columnDefinition = "TEXT")
    private String statementOfPurpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(name = "submitted_at")
    private Instant submittedAt = Instant.now();

    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "review_decision", length = 32)
    private String reviewDecision;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Application() {
    }

    public Application(String applicationNumber, String fullName, String email, String phone, String experienceLevel) {
        this.applicationNumber = applicationNumber;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.experienceLevel = experienceLevel;
        this.status = ApplicationStatus.SUBMITTED;
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

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getApplicantUserId() {
        return applicantUserId;
    }

    public void setApplicantUserId(UUID applicantUserId) {
        this.applicantUserId = applicantUserId;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getStatementOfPurpose() {
        return statementOfPurpose;
    }

    public void setStatementOfPurpose(String statementOfPurpose) {
        this.statementOfPurpose = statementOfPurpose;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public UUID getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(UUID reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewDecision() {
        return reviewDecision;
    }

    public void setReviewDecision(String reviewDecision) {
        this.reviewDecision = reviewDecision;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
