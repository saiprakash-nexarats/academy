package com.nexarats.academy.learning.dto;

import java.util.UUID;

public class PlaybackCheckpointResponse {

    private UUID lessonId;
    private UUID courseId;
    private int lastPlaybackPositionSeconds;
    private int watchTimeSeconds;
    private int lessonCompletionPercentage;
    @com.fasterxml.jackson.annotation.JsonProperty("isLessonCompleted")
    private boolean isLessonCompleted;
    private int courseCompletionPercentage;
    private String message;

    public PlaybackCheckpointResponse() {}

    public PlaybackCheckpointResponse(UUID lessonId, UUID courseId, int lastPlaybackPositionSeconds,
                                      int watchTimeSeconds, int lessonCompletionPercentage, boolean isLessonCompleted,
                                      int courseCompletionPercentage, String message) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.lastPlaybackPositionSeconds = lastPlaybackPositionSeconds;
        this.watchTimeSeconds = watchTimeSeconds;
        this.lessonCompletionPercentage = lessonCompletionPercentage;
        this.isLessonCompleted = isLessonCompleted;
        this.courseCompletionPercentage = courseCompletionPercentage;
        this.message = message;
    }

    public UUID getLessonId() { return lessonId; }
    public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }
    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public int getLastPlaybackPositionSeconds() { return lastPlaybackPositionSeconds; }
    public void setLastPlaybackPositionSeconds(int lastPlaybackPositionSeconds) { this.lastPlaybackPositionSeconds = lastPlaybackPositionSeconds; }
    public int getWatchTimeSeconds() { return watchTimeSeconds; }
    public void setWatchTimeSeconds(int watchTimeSeconds) { this.watchTimeSeconds = watchTimeSeconds; }
    public int getLessonCompletionPercentage() { return lessonCompletionPercentage; }
    public void setLessonCompletionPercentage(int lessonCompletionPercentage) { this.lessonCompletionPercentage = lessonCompletionPercentage; }

    @com.fasterxml.jackson.annotation.JsonProperty("isLessonCompleted")
    public boolean isLessonCompleted() { return isLessonCompleted; }

    @com.fasterxml.jackson.annotation.JsonProperty("isLessonCompleted")
    public void setLessonCompleted(boolean lessonCompleted) { isLessonCompleted = lessonCompleted; }
    public int getCourseCompletionPercentage() { return courseCompletionPercentage; }
    public void setCourseCompletionPercentage(int courseCompletionPercentage) { this.courseCompletionPercentage = courseCompletionPercentage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
