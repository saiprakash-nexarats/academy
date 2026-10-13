package com.nexarats.academy.learning.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_activity_records")
public class LearningActivityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "learner_user_id", nullable = false)
    private UUID learnerUserId;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "activity_type", nullable = false, length = 64)
    private String activityType;

    @Column(name = "playback_position_seconds")
    private int playbackPositionSeconds = 0;

    @Column(name = "watch_time_delta_seconds")
    private int watchTimeDeltaSeconds = 0;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "jsonb")
    private String metadataJson;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LearningActivityRecord() {
    }

    public LearningActivityRecord(UUID learnerUserId, UUID enrollmentId, UUID courseId, UUID lessonId, String activityType) {
        this.learnerUserId = learnerUserId;
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.activityType = activityType;
        this.createdAt = Instant.now();
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

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public int getPlaybackPositionSeconds() {
        return playbackPositionSeconds;
    }

    public void setPlaybackPositionSeconds(int playbackPositionSeconds) {
        this.playbackPositionSeconds = playbackPositionSeconds;
    }

    public int getWatchTimeDeltaSeconds() {
        return watchTimeDeltaSeconds;
    }

    public void setWatchTimeDeltaSeconds(int watchTimeDeltaSeconds) {
        this.watchTimeDeltaSeconds = watchTimeDeltaSeconds;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
