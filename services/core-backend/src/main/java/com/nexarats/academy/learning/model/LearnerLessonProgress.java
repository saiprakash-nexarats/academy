package com.nexarats.academy.learning.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learner_lesson_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"learner_user_id", "lesson_id", "enrollment_id"})
})
public class LearnerLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "learner_user_id", nullable = false)
    private UUID learnerUserId;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "curriculum_version_id")
    private UUID curriculumVersionId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "NOT_STARTED";

    @Column(name = "watch_time_seconds", nullable = false)
    private int watchTimeSeconds = 0;

    @Column(name = "last_playback_position_seconds", nullable = false)
    private int lastPlaybackPositionSeconds = 0;

    @Column(name = "completion_percentage", nullable = false)
    private int completionPercentage = 0;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public LearnerLessonProgress() {
    }

    public LearnerLessonProgress(UUID learnerUserId, UUID enrollmentId, UUID courseId, UUID lessonId) {
        this.learnerUserId = learnerUserId;
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.status = "NOT_STARTED";
        this.lastActivityAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getLearnerUserId() {
        return learnerUserId;
    }

    public void setLearnerUserId(UUID learnerUserId) {
        this.learnerUserId = learnerUserId;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(UUID enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public void setLessonId(UUID lessonId) {
        this.lessonId = lessonId;
    }

    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }

    public void setCurriculumVersionId(UUID curriculumVersionId) {
        this.curriculumVersionId = curriculumVersionId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getWatchTimeSeconds() {
        return watchTimeSeconds;
    }

    public void setWatchTimeSeconds(int watchTimeSeconds) {
        this.watchTimeSeconds = Math.max(0, watchTimeSeconds);
    }

    public int getLastPlaybackPositionSeconds() {
        return lastPlaybackPositionSeconds;
    }

    public void setLastPlaybackPositionSeconds(int lastPlaybackPositionSeconds) {
        this.lastPlaybackPositionSeconds = Math.max(0, lastPlaybackPositionSeconds);
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = Math.max(0, Math.min(100, completionPercentage));
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
