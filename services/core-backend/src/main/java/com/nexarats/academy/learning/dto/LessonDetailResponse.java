package com.nexarats.academy.learning.dto;

import java.util.UUID;

public class LessonDetailResponse {

    private UUID lessonId;
    private UUID courseId;
    private String courseTitle;
    private UUID moduleId;
    private String moduleTitle;
    private String lessonTitle;
    private String summary;
    private String lessonType;
    private int durationMinutes;
    private boolean isPreviewable;
    private boolean isLocked;
    private boolean isCompleted;
    private int lastPlaybackPositionSeconds;
    private int watchTimeSeconds;
    private String contentBody;
    private UUID previousLessonId;
    private UUID nextLessonId;
    private boolean hasMediaStream;

    public LessonDetailResponse() {}

    public UUID getLessonId() { return lessonId; }
    public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }
    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public UUID getModuleId() { return moduleId; }
    public void setModuleId(UUID moduleId) { this.moduleId = moduleId; }
    public String getModuleTitle() { return moduleTitle; }
    public void setModuleTitle(String moduleTitle) { this.moduleTitle = moduleTitle; }
    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getLessonType() { return lessonType; }
    public void setLessonType(String lessonType) { this.lessonType = lessonType; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public boolean isPreviewable() { return isPreviewable; }
    public void setPreviewable(boolean previewable) { isPreviewable = previewable; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public int getLastPlaybackPositionSeconds() { return lastPlaybackPositionSeconds; }
    public void setLastPlaybackPositionSeconds(int lastPlaybackPositionSeconds) { this.lastPlaybackPositionSeconds = lastPlaybackPositionSeconds; }
    public int getWatchTimeSeconds() { return watchTimeSeconds; }
    public void setWatchTimeSeconds(int watchTimeSeconds) { this.watchTimeSeconds = watchTimeSeconds; }
    public String getContentBody() { return contentBody; }
    public void setContentBody(String contentBody) { this.contentBody = contentBody; }
    public UUID getPreviousLessonId() { return previousLessonId; }
    public void setPreviousLessonId(UUID previousLessonId) { this.previousLessonId = previousLessonId; }
    public UUID getNextLessonId() { return nextLessonId; }
    public void setNextLessonId(UUID nextLessonId) { this.nextLessonId = nextLessonId; }
    public boolean isHasMediaStream() { return hasMediaStream; }
    public void setHasMediaStream(boolean hasMediaStream) { this.hasMediaStream = hasMediaStream; }
}
