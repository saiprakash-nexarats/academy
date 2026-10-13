package com.nexarats.academy.learning.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CourseProgressResponse {

    private UUID courseId;
    private String courseTitle;
    private String enrollmentStatus;
    private int completionPercentage;
    private int completedLessonsCount;
    private int totalLessonsCount;
    private UUID currentLessonId;
    private String status;
    private Instant completedAt;
    private List<LearnerSyllabusResponse.LearnerLessonDto> lessonProgresses;

    public CourseProgressResponse() {}

    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public List<LearnerSyllabusResponse.LearnerLessonDto> getLessonProgresses() { return lessonProgresses; }
    public void setLessonProgresses(List<LearnerSyllabusResponse.LearnerLessonDto> lessonProgresses) { this.lessonProgresses = lessonProgresses; }
}
