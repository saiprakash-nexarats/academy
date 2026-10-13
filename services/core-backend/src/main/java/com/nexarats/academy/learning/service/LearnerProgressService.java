package com.nexarats.academy.learning.service;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.learning.dto.CourseProgressResponse;
import com.nexarats.academy.learning.dto.LearnerSyllabusResponse;
import com.nexarats.academy.learning.dto.PlaybackCheckpointRequest;
import com.nexarats.academy.learning.dto.PlaybackCheckpointResponse;
import com.nexarats.academy.learning.model.LearnerCourseProgress;
import com.nexarats.academy.learning.model.LearnerLessonProgress;
import com.nexarats.academy.learning.model.LearningActivityRecord;
import com.nexarats.academy.learning.model.LessonMediaAsset;
import com.nexarats.academy.learning.repository.LearnerCourseProgressRepository;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.repository.LearningActivityRecordRepository;
import com.nexarats.academy.learning.repository.LessonMediaAssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class LearnerProgressService {

    private static final Logger log = LoggerFactory.getLogger(LearnerProgressService.class);

    private final LearnerEntitlementService entitlementService;
    private final LearnerCourseProgressRepository courseProgressRepository;
    private final LearnerLessonProgressRepository lessonProgressRepository;
    private final LearningActivityRecordRepository activityRecordRepository;
    private final LessonMediaAssetRepository mediaAssetRepository;
    private final CourseRepository courseRepository;

    public LearnerProgressService(LearnerEntitlementService entitlementService,
                                  LearnerCourseProgressRepository courseProgressRepository,
                                  LearnerLessonProgressRepository lessonProgressRepository,
                                  LearningActivityRecordRepository activityRecordRepository,
                                  LessonMediaAssetRepository mediaAssetRepository,
                                  CourseRepository courseRepository) {
        this.entitlementService = entitlementService;
        this.courseProgressRepository = courseProgressRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.activityRecordRepository = activityRecordRepository;
        this.mediaAssetRepository = mediaAssetRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseProgressResponse getCourseProgress(UUID learnerUserId, UUID courseId) {
        Enrollment enrollment = entitlementService.verifyCourseEntitlement(learnerUserId, courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found: " + courseId));

        CurriculumVersion version = course.getPublishedVersion();
        if (version == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course has no published curriculum version.");
        }

        LearnerCourseProgress courseProg = courseProgressRepository
                .findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId())
                .orElseGet(() -> {
                    LearnerCourseProgress created = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, enrollment.getTenantId());
                    created.setCurriculumVersionId(version.getId());
                    return courseProgressRepository.save(created);
                });

        // Compute lesson progress items
        List<LearnerLessonProgress> existingLessonProgs = lessonProgressRepository.findByLearnerUserIdAndCourseId(learnerUserId, courseId);
        Map<UUID, LearnerLessonProgress> progressByLessonId = new HashMap<>();
        for (LearnerLessonProgress lp : existingLessonProgs) {
            progressByLessonId.put(lp.getLessonId(), lp);
        }

        List<LearnerSyllabusResponse.LearnerLessonDto> dtoList = new ArrayList<>();
        int totalLessons = 0;
        int completedLessons = 0;

        for (CurriculumModule module : version.getModules()) {
            for (Lesson l : module.getLessons()) {
                totalLessons++;
                LearnerLessonProgress lp = progressByLessonId.get(l.getId());
                boolean isCompleted = lp != null && lp.isCompleted();
                if (isCompleted) {
                    completedLessons++;
                }

                LearnerSyllabusResponse.LearnerLessonDto dto = new LearnerSyllabusResponse.LearnerLessonDto();
                dto.setLessonId(l.getId());
                dto.setLessonTitle(l.getLessonTitle());
                dto.setSummary(l.getSummary());
                dto.setLessonType(l.getLessonType());
                dto.setDurationMinutes(l.getDurationMinutes());
                dto.setDisplayOrder(l.getDisplayOrder());
                dto.setPreviewable(l.isPreviewable());
                dto.setLocked(false); // Entitled learner can access all published lessons
                dto.setCompleted(isCompleted);
                dto.setLastPlaybackPositionSeconds(lp != null ? lp.getLastPlaybackPositionSeconds() : 0);
                dto.setWatchTimeSeconds(lp != null ? lp.getWatchTimeSeconds() : 0);
                dtoList.add(dto);
            }
        }

        int calculatedPercentage = totalLessons > 0 ? (int) ((completedLessons * 100L) / totalLessons) : 0;
        if (courseProg.getCompletionPercentage() != calculatedPercentage) {
            courseProg.setCompletionPercentage(calculatedPercentage);
            if (calculatedPercentage == 100 && courseProg.getCompletedAt() == null) {
                courseProg.setStatus("COMPLETED");
                courseProg.setCompletedAt(Instant.now());
            }
            courseProgressRepository.save(courseProg);
        }

        CourseProgressResponse resp = new CourseProgressResponse();
        resp.setCourseId(courseId);
        resp.setCourseTitle(course.getTitle());
        resp.setEnrollmentStatus(enrollment.getStatus().name());
        resp.setCompletionPercentage(courseProg.getCompletionPercentage());
        resp.setCompletedLessonsCount(completedLessons);
        resp.setTotalLessonsCount(totalLessons);
        resp.setCurrentLessonId(courseProg.getCurrentLessonId());
        resp.setStatus(courseProg.getStatus());
        resp.setCompletedAt(courseProg.getCompletedAt());
        resp.setLessonProgresses(dtoList);

        return resp;
    }

    @Transactional
    public PlaybackCheckpointResponse recordCheckpoint(UUID learnerUserId, UUID lessonId, PlaybackCheckpointRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Checkpoint request cannot be null.");
        }
        if (request.getPlaybackPositionSeconds() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playback position cannot be negative.");
        }
        if (request.getWatchTimeDeltaSeconds() < 0 || request.getWatchTimeDeltaSeconds() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unrealistic watch time delta: must be between 0 and 300 seconds.");
        }

        LearnerEntitlementService.LessonAndEntitlement le = entitlementService.verifyLessonEntitlement(learnerUserId, lessonId);
        Lesson lesson = le.lesson();
        Course course = le.course();
        Enrollment enrollment = le.enrollment();

        // Check idempotency key to prevent replay attacks
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<LearningActivityRecord> existingRec = activityRecordRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingRec.isPresent()) {
                log.info("Duplicate checkpoint idempotency key {}. Returning current progress.", request.getIdempotencyKey());
                LearnerLessonProgress currentLp = lessonProgressRepository
                        .findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId())
                        .orElse(null);
                int lessonPct = currentLp != null ? currentLp.getCompletionPercentage() : 0;
                boolean completed = currentLp != null && currentLp.isCompleted();
                return new PlaybackCheckpointResponse(lessonId, course.getId(),
                        currentLp != null ? currentLp.getLastPlaybackPositionSeconds() : 0,
                        currentLp != null ? currentLp.getWatchTimeSeconds() : 0,
                        lessonPct, completed, 0, "Idempotent duplicate ignored.");
            }
        }

        // Resolve or create lesson progress
        LearnerLessonProgress lp = lessonProgressRepository
                .findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId())
                .orElseGet(() -> {
                    LearnerLessonProgress created = new LearnerLessonProgress(learnerUserId, enrollment.getId(), course.getId(), lessonId);
                    created.setTenantId(enrollment.getTenantId());
                    created.setCurriculumVersionId(lesson.getModule().getVersion().getId());
                    return created;
                });

        lp.setLastPlaybackPositionSeconds(request.getPlaybackPositionSeconds());
        lp.setWatchTimeSeconds(lp.getWatchTimeSeconds() + request.getWatchTimeDeltaSeconds());

        int durationSeconds = lesson.getDurationMinutes() * 60;
        Optional<LessonMediaAsset> mediaOpt = mediaAssetRepository.findFirstByLessonId(lessonId);
        if (mediaOpt.isPresent() && mediaOpt.get().getDurationSeconds() > 0) {
            durationSeconds = mediaOpt.get().getDurationSeconds();
        }

        // Completion heuristic: 90% position reached or 80% total watch time accumulated
        boolean shouldComplete = request.getPlaybackPositionSeconds() >= (durationSeconds * 0.90) ||
                lp.getWatchTimeSeconds() >= (durationSeconds * 0.80);

        if (shouldComplete && !lp.isCompleted()) {
            lp.setCompleted(true);
            lp.setStatus("COMPLETED");
            lp.setCompletionPercentage(100);
            lp.setCompletedAt(Instant.now());
        } else if (!lp.isCompleted()) {
            int calculated = (int) ((request.getPlaybackPositionSeconds() * 100L) / Math.max(1, durationSeconds));
            lp.setCompletionPercentage(Math.min(99, Math.max(lp.getCompletionPercentage(), calculated)));
            lp.setStatus("IN_PROGRESS");
        }

        LearnerLessonProgress savedLp = lessonProgressRepository.save(lp);

        // Update Course Progress
        LearnerCourseProgress courseProg = courseProgressRepository
                .findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, course.getId(), enrollment.getId())
                .orElseGet(() -> {
                    LearnerCourseProgress created = new LearnerCourseProgress(learnerUserId, enrollment.getId(), course.getId(), enrollment.getTenantId());
                    created.setCurriculumVersionId(lesson.getModule().getVersion().getId());
                    return created;
                });

        courseProg.setCurrentLessonId(lessonId);
        courseProg.setLastActivityAt(Instant.now());

        // Recalculate course percentage
        long completedCount = lessonProgressRepository.countByLearnerUserIdAndCourseIdAndIsCompletedTrue(learnerUserId, course.getId());
        int totalLessons = 0;
        for (CurriculumModule mod : lesson.getModule().getVersion().getModules()) {
            totalLessons += mod.getLessons().size();
        }
        int coursePercentage = totalLessons > 0 ? (int) ((completedCount * 100L) / totalLessons) : 0;
        courseProg.setCompletionPercentage(coursePercentage);
        if (coursePercentage == 100 && courseProg.getCompletedAt() == null) {
            courseProg.setStatus("COMPLETED");
            courseProg.setCompletedAt(Instant.now());
        }
        courseProgressRepository.save(courseProg);

        // Persist activity record
        LearningActivityRecord activity = new LearningActivityRecord(learnerUserId, enrollment.getId(), course.getId(), lessonId, "PLAYBACK_CHECKPOINT");
        activity.setPlaybackPositionSeconds(request.getPlaybackPositionSeconds());
        activity.setWatchTimeDeltaSeconds(request.getWatchTimeDeltaSeconds());
        activity.setIdempotencyKey(request.getIdempotencyKey());
        activityRecordRepository.save(activity);

        return new PlaybackCheckpointResponse(
                lessonId,
                course.getId(),
                savedLp.getLastPlaybackPositionSeconds(),
                savedLp.getWatchTimeSeconds(),
                savedLp.getCompletionPercentage(),
                savedLp.isCompleted(),
                courseProg.getCompletionPercentage(),
                "Progress checkpoint successfully recorded."
        );
    }

    @Transactional
    public PlaybackCheckpointResponse markLessonComplete(UUID learnerUserId, UUID lessonId) {
        LearnerEntitlementService.LessonAndEntitlement le = entitlementService.verifyLessonEntitlement(learnerUserId, lessonId);
        Lesson lesson = le.lesson();
        Course course = le.course();
        Enrollment enrollment = le.enrollment();

        LearnerLessonProgress lp = lessonProgressRepository
                .findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId())
                .orElseGet(() -> {
                    LearnerLessonProgress created = new LearnerLessonProgress(learnerUserId, enrollment.getId(), course.getId(), lessonId);
                    created.setTenantId(enrollment.getTenantId());
                    created.setCurriculumVersionId(lesson.getModule().getVersion().getId());
                    return created;
                });

        lp.setCompleted(true);
        lp.setStatus("COMPLETED");
        lp.setCompletionPercentage(100);
        lp.setCompletedAt(Instant.now());
        LearnerLessonProgress savedLp = lessonProgressRepository.save(lp);

        LearnerCourseProgress courseProg = courseProgressRepository
                .findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, course.getId(), enrollment.getId())
                .orElseGet(() -> {
                    LearnerCourseProgress created = new LearnerCourseProgress(learnerUserId, enrollment.getId(), course.getId(), enrollment.getTenantId());
                    created.setCurriculumVersionId(lesson.getModule().getVersion().getId());
                    return created;
                });

        courseProg.setCurrentLessonId(lessonId);
        long completedCount = lessonProgressRepository.countByLearnerUserIdAndCourseIdAndIsCompletedTrue(learnerUserId, course.getId());
        int totalLessons = 0;
        for (CurriculumModule mod : lesson.getModule().getVersion().getModules()) {
            totalLessons += mod.getLessons().size();
        }
        int coursePercentage = totalLessons > 0 ? (int) ((completedCount * 100L) / totalLessons) : 0;
        courseProg.setCompletionPercentage(coursePercentage);
        if (coursePercentage == 100 && courseProg.getCompletedAt() == null) {
            courseProg.setStatus("COMPLETED");
            courseProg.setCompletedAt(Instant.now());
        }
        courseProgressRepository.save(courseProg);

        // Record activity
        LearningActivityRecord activity = new LearningActivityRecord(learnerUserId, enrollment.getId(), course.getId(), lessonId, "LESSON_COMPLETED");
        activityRecordRepository.save(activity);

        return new PlaybackCheckpointResponse(
                lessonId,
                course.getId(),
                savedLp.getLastPlaybackPositionSeconds(),
                savedLp.getWatchTimeSeconds(),
                100,
                true,
                courseProg.getCompletionPercentage(),
                "Lesson marked as completed."
        );
    }
}
