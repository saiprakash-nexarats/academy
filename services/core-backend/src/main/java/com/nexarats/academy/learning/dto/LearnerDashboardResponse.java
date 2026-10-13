package com.nexarats.academy.learning.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class LearnerDashboardResponse {

    private UUID learnerUserId;
    private String fullName;
    private String email;
    private int enrolledCoursesCount;
    private int completedCoursesCount;
    private int totalWatchTimeMinutes;
    private ContinueLearningTarget continueLearningTarget;
    private List<EnrolledCourseSummary> enrolledCourses;
    private List<EnrolledProgramSummary> enrolledPrograms;

    public LearnerDashboardResponse() {
    }

    public LearnerDashboardResponse(UUID learnerUserId, String fullName, String email,
                                    int enrolledCoursesCount, int completedCoursesCount, int totalWatchTimeMinutes,
                                    ContinueLearningTarget continueLearningTarget,
                                    List<EnrolledCourseSummary> enrolledCourses,
                                    List<EnrolledProgramSummary> enrolledPrograms) {
        this.learnerUserId = learnerUserId;
        this.fullName = fullName;
        this.email = email;
        this.enrolledCoursesCount = enrolledCoursesCount;
        this.completedCoursesCount = completedCoursesCount;
        this.totalWatchTimeMinutes = totalWatchTimeMinutes;
        this.continueLearningTarget = continueLearningTarget;
        this.enrolledCourses = enrolledCourses;
        this.enrolledPrograms = enrolledPrograms;
    }

    public UUID getLearnerUserId() {
        return learnerUserId;
    }

    public void setLearnerUserId(UUID learnerUserId) {
        this.learnerUserId = learnerUserId;
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

    public int getEnrolledCoursesCount() {
        return enrolledCoursesCount;
    }

    public void setEnrolledCoursesCount(int enrolledCoursesCount) {
        this.enrolledCoursesCount = enrolledCoursesCount;
    }

    public int getCompletedCoursesCount() {
        return completedCoursesCount;
    }

    public void setCompletedCoursesCount(int completedCoursesCount) {
        this.completedCoursesCount = completedCoursesCount;
    }

    public int getTotalWatchTimeMinutes() {
        return totalWatchTimeMinutes;
    }

    public void setTotalWatchTimeMinutes(int totalWatchTimeMinutes) {
        this.totalWatchTimeMinutes = totalWatchTimeMinutes;
    }

    public ContinueLearningTarget getContinueLearningTarget() {
        return continueLearningTarget;
    }

    public void setContinueLearningTarget(ContinueLearningTarget continueLearningTarget) {
        this.continueLearningTarget = continueLearningTarget;
    }

    public List<EnrolledCourseSummary> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<EnrolledCourseSummary> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public List<EnrolledProgramSummary> getEnrolledPrograms() {
        return enrolledPrograms;
    }

    public void setEnrolledPrograms(List<EnrolledProgramSummary> enrolledPrograms) {
        this.enrolledPrograms = enrolledPrograms;
    }

    public static class ContinueLearningTarget {
        private UUID courseId;
        private String courseSlug;
        private String courseTitle;
        private UUID lessonId;
        private String lessonTitle;
        private int lastPlaybackPositionSeconds;
        private int durationMinutes;

        public ContinueLearningTarget() {}

        public ContinueLearningTarget(UUID courseId, String courseSlug, String courseTitle,
                                      UUID lessonId, String lessonTitle,
                                      int lastPlaybackPositionSeconds, int durationMinutes) {
            this.courseId = courseId;
            this.courseSlug = courseSlug;
            this.courseTitle = courseTitle;
            this.lessonId = lessonId;
            this.lessonTitle = lessonTitle;
            this.lastPlaybackPositionSeconds = lastPlaybackPositionSeconds;
            this.durationMinutes = durationMinutes;
        }

        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public String getCourseSlug() { return courseSlug; }
        public void setCourseSlug(String courseSlug) { this.courseSlug = courseSlug; }
        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
        public UUID getLessonId() { return lessonId; }
        public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }
        public String getLessonTitle() { return lessonTitle; }
        public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
        public int getLastPlaybackPositionSeconds() { return lastPlaybackPositionSeconds; }
        public void setLastPlaybackPositionSeconds(int lastPlaybackPositionSeconds) { this.lastPlaybackPositionSeconds = lastPlaybackPositionSeconds; }
        public int getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    }

    public static class EnrolledCourseSummary {
        private UUID courseId;
        private String slug;
        private String title;
        private String category;
        private String level;
        private String imageAssetKey;
        private String instructorName;
        private String instructorCompany;
        private String enrollmentNumber;
        private String enrollmentStatus;
        private int completionPercentage;
        private int completedLessonsCount;
        private int totalLessonsCount;
        private UUID currentLessonId;
        private String currentLessonTitle;
        private Instant lastActivityAt;

        public EnrolledCourseSummary() {}

        public UUID getCourseId() { return courseId; }
        public void setCourseId(UUID courseId) { this.courseId = courseId; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getImageAssetKey() { return imageAssetKey; }
        public void setImageAssetKey(String imageAssetKey) { this.imageAssetKey = imageAssetKey; }
        public String getInstructorName() { return instructorName; }
        public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
        public String getInstructorCompany() { return instructorCompany; }
        public void setInstructorCompany(String instructorCompany) { this.instructorCompany = instructorCompany; }
        public String getEnrollmentNumber() { return enrollmentNumber; }
        public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }
        public String getEnrollmentStatus() { return enrollmentStatus; }
        public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }
        public int getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
        public int getCompletedLessonsCount() { return completedLessonsCount; }
        public void setCompletedLessonsCount(int completedLessonsCount) { this.completedLessonsCount = completedLessonsCount; }
        public int getTotalLessonsCount() { return totalLessonsCount; }
        public void setTotalLessonsCount(int totalLessonsCount) { this.totalLessonsCount = totalLessonsCount; }
        public UUID getCurrentLessonId() { return currentLessonId; }
        public void setCurrentLessonId(UUID currentLessonId) { this.currentLessonId = currentLessonId; }
        public String getCurrentLessonTitle() { return currentLessonTitle; }
        public void setCurrentLessonTitle(String currentLessonTitle) { this.currentLessonTitle = currentLessonTitle; }
        public Instant getLastActivityAt() { return lastActivityAt; }
        public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }
    }

    public static class EnrolledProgramSummary {
        private UUID programId;
        private String slug;
        private String title;
        private String duration;
        private String enrollmentNumber;
        private String cohortCode;
        private int completionPercentage;
        private int totalCoursesCount;
        private int completedCoursesCount;

        public EnrolledProgramSummary() {}

        public UUID getProgramId() { return programId; }
        public void setProgramId(UUID programId) { this.programId = programId; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getEnrollmentNumber() { return enrollmentNumber; }
        public void setEnrollmentNumber(String enrollmentNumber) { this.enrollmentNumber = enrollmentNumber; }
        public String getCohortCode() { return cohortCode; }
        public void setCohortCode(String cohortCode) { this.cohortCode = cohortCode; }
        public int getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
        public int getTotalCoursesCount() { return totalCoursesCount; }
        public void setTotalCoursesCount(int totalCoursesCount) { this.totalCoursesCount = totalCoursesCount; }
        public int getCompletedCoursesCount() { return completedCoursesCount; }
        public void setCompletedCoursesCount(int completedCoursesCount) { this.completedCoursesCount = completedCoursesCount; }
    }
}
