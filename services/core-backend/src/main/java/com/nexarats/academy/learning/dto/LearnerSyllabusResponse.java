package com.nexarats.academy.learning.dto;

import java.util.List;
import java.util.UUID;

public class LearnerSyllabusResponse {

    private UUID courseId;
    private String courseTitle;
    private String versionLabel;
    private int totalLessonsCount;
    private int completedLessonsCount;
    private int overallProgressPercentage;
    private List<LearnerModuleDto> modules;

    public LearnerSyllabusResponse() {}

    public LearnerSyllabusResponse(UUID courseId, String courseTitle, String versionLabel,
                                   int totalLessonsCount, int completedLessonsCount, int overallProgressPercentage,
                                   List<LearnerModuleDto> modules) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.versionLabel = versionLabel;
        this.totalLessonsCount = totalLessonsCount;
        this.completedLessonsCount = completedLessonsCount;
        this.overallProgressPercentage = overallProgressPercentage;
        this.modules = modules;
    }

    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }
    public int getTotalLessonsCount() { return totalLessonsCount; }
    public void setTotalLessonsCount(int totalLessonsCount) { this.totalLessonsCount = totalLessonsCount; }
    public int getCompletedLessonsCount() { return completedLessonsCount; }
    public void setCompletedLessonsCount(int completedLessonsCount) { this.completedLessonsCount = completedLessonsCount; }
    public int getOverallProgressPercentage() { return overallProgressPercentage; }
    public void setOverallProgressPercentage(int overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }
    public List<LearnerModuleDto> getModules() { return modules; }
    public void setModules(List<LearnerModuleDto> modules) { this.modules = modules; }

    public static class LearnerModuleDto {
        private UUID moduleId;
        private String moduleNumber;
        private String title;
        private String durationLabel;
        private int displayOrder;
        private List<LearnerLessonDto> lessons;

        public LearnerModuleDto() {}

        public UUID getModuleId() { return moduleId; }
        public void setModuleId(UUID moduleId) { this.moduleId = moduleId; }
        public String getModuleNumber() { return moduleNumber; }
        public void setModuleNumber(String moduleNumber) { this.moduleNumber = moduleNumber; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDurationLabel() { return durationLabel; }
        public void setDurationLabel(String durationLabel) { this.durationLabel = durationLabel; }
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
        public List<LearnerLessonDto> getLessons() { return lessons; }
        public void setLessons(List<LearnerLessonDto> lessons) { this.lessons = lessons; }
    }

    public static class LearnerLessonDto {
        private UUID lessonId;
        private String lessonTitle;
        private String summary;
        private String lessonType;
        private int durationMinutes;
        private int displayOrder;
        private boolean isPreviewable;
        private boolean isLocked;
        private boolean isCompleted;
        private int lastPlaybackPositionSeconds;
        private int watchTimeSeconds;

        public LearnerLessonDto() {}

        public UUID getLessonId() { return lessonId; }
        public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }
        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getLessonType() { return lessonType; }
        public void setLessonType(String lessonType) { this.lessonType = lessonType; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
        public int getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
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
    }
}
