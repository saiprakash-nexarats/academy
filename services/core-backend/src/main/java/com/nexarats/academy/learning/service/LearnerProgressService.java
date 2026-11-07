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
import com.nexarats.academy.learning.model.PlaybackSession;
import com.nexarats.academy.learning.repository.LearnerCourseProgressRepository;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.repository.LearningActivityRecordRepository;
import com.nexarats.academy.learning.repository.LessonMediaAssetRepository;
import com.nexarats.academy.learning.repository.PlaybackSessionRepository;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
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
    private final PlaybackSessionRepository playbackSessionRepository;
    private final CourseRepository courseRepository;
    private final StudentProfileRepository studentProfileRepository;

    public LearnerProgressService(LearnerEntitlementService entitlementService,
                                  LearnerCourseProgressRepository courseProgressRepository,
                                  LearnerLessonProgressRepository lessonProgressRepository,
                                  LearningActivityRecordRepository activityRecordRepository,
                                  LessonMediaAssetRepository mediaAssetRepository,
                                  PlaybackSessionRepository playbackSessionRepository,
                                  CourseRepository courseRepository,
                                  StudentProfileRepository studentProfileRepository) {
        this.entitlementService = Objects.requireNonNull(entitlementService, "entitlementService must not be null");
        this.courseProgressRepository = Objects.requireNonNull(courseProgressRepository, "courseProgressRepository must not be null");
        this.lessonProgressRepository = Objects.requireNonNull(lessonProgressRepository, "lessonProgressRepository must not be null");
        this.activityRecordRepository = Objects.requireNonNull(activityRecordRepository, "activityRecordRepository must not be null");
        this.mediaAssetRepository = Objects.requireNonNull(mediaAssetRepository, "mediaAssetRepository must not be null");
        this.playbackSessionRepository = Objects.requireNonNull(playbackSessionRepository, "playbackSessionRepository must not be null");
        this.courseRepository = Objects.requireNonNull(courseRepository, "courseRepository must not be null");
        this.studentProfileRepository = Objects.requireNonNull(studentProfileRepository, "studentProfileRepository must not be null");
    }

    private boolean isStaffOrAdmin(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream().anyMatch(r -> {
            String role = r.toUpperCase();
            return role.equals("ROLE_SUPER_ADMIN")
                    || role.equals("SUPER_ADMIN")
                    || role.equals("ROLE_ACADEMY_ADMIN")
                    || role.equals("ACADEMY_ADMIN")
                    || role.equals("ROLE_ADMIN")
                    || role.equals("ADMIN")
                    || role.equals("ROLE_INSTRUCTOR")
                    || role.equals("INSTRUCTOR")
                    || role.equals("ROLE_MENTOR")
                    || role.equals("MENTOR");
        });
    }

    @Transactional
    public CourseProgressResponse getCourseProgress(UUID learnerUserId, UUID courseId) {
        return getCourseProgress(learnerUserId, courseId, null);
    }

    @Transactional
    public CourseProgressResponse getCourseProgress(UUID learnerUserId, UUID courseId, UUID tenantId) {
        Enrollment enrollment = entitlementService.verifyCourseEntitlement(learnerUserId, courseId, tenantId);
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

        List<LearnerLessonProgress> lessonProgs = lessonProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId());
        Map<UUID, LearnerLessonProgress> progressByLessonId = new HashMap<>();
        for (LearnerLessonProgress lp : lessonProgs) {
            progressByLessonId.put(lp.getLessonId(), lp);
        }

        List<LearnerSyllabusResponse.LearnerLessonDto> dtoList = new ArrayList<>();
        int totalLessons = 0;
        int completedCount = 0;

        for (CurriculumModule mod : version.getModules()) {
            for (Lesson l : mod.getLessons()) {
                totalLessons++;
                LearnerLessonProgress lp = progressByLessonId.get(l.getId());
                boolean completed = lp != null && lp.isCompleted();
                if (completed) completedCount++;

                int pos = lp != null ? lp.getLastPlaybackPositionSeconds() : 0;
                int watch = lp != null ? lp.getWatchTimeSeconds() : 0;

                LearnerSyllabusResponse.LearnerLessonDto dto = new LearnerSyllabusResponse.LearnerLessonDto();
                dto.setLessonId(l.getId());
                dto.setLessonTitle(l.getLessonTitle());
                dto.setSummary(l.getSummary());
                dto.setLessonType(l.getLessonType());
                dto.setDurationMinutes(l.getDurationMinutes());
                dto.setDisplayOrder(l.getDisplayOrder());
                dto.setPreviewable(l.isPreviewable());
                dto.setLocked(false);
                dto.setCompleted(completed);
                dto.setLastPlaybackPositionSeconds(pos);
                dto.setWatchTimeSeconds(watch);
                dtoList.add(dto);
            }
        }

        int percentage = totalLessons > 0 ? (int) ((completedCount * 100L) / totalLessons) : 0;
        courseProg.setCompletionPercentage(percentage);
        if (percentage == 100 && courseProg.getCompletedAt() == null) {
            courseProg.setStatus("COMPLETED");
            courseProg.setCompletedAt(Instant.now());
        }
        courseProgressRepository.save(courseProg);

        CourseProgressResponse resp = new CourseProgressResponse();
        resp.setCourseId(course.getId());
        resp.setCourseTitle(course.getTitle());
        resp.setEnrollmentStatus(enrollment.getStatus().name());
        resp.setCompletionPercentage(courseProg.getCompletionPercentage());
        resp.setCompletedLessonsCount(completedCount);
        resp.setTotalLessonsCount(totalLessons);
        resp.setCurrentLessonId(courseProg.getCurrentLessonId());
        resp.setStatus(courseProg.getStatus());
        resp.setCompletedAt(courseProg.getCompletedAt());
        resp.setLessonProgresses(dtoList);
        return resp;
    }

    @Transactional
    public PlaybackCheckpointResponse recordCheckpoint(UUID learnerUserId, UUID lessonId, PlaybackCheckpointRequest request) {
        return recordCheckpoint(learnerUserId, lessonId, request, null, null);
    }

    @Transactional
    public PlaybackCheckpointResponse recordCheckpoint(UUID learnerUserId, UUID lessonId, PlaybackCheckpointRequest request, UUID tenantId) {
        return recordCheckpoint(learnerUserId, lessonId, request, tenantId, null);
    }

    @Transactional
    public PlaybackCheckpointResponse recordCheckpoint(UUID learnerUserId, UUID lessonId, PlaybackCheckpointRequest request, UUID tenantId, Collection<String> roles) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Checkpoint request cannot be null.");
        }
        if (request.getPlaybackPositionSeconds() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playback position cannot be negative.");
        }
        if (request.getWatchTimeDeltaSeconds() < 0 || request.getWatchTimeDeltaSeconds() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unrealistic watch time delta: must be between 0 and 300 seconds.");
        }

        // Student academic onboarding enforcement:
        // Staff/instructors/administrators are exempted when previewing/testing content.
        // For student learners, onboarding profile MUST exist and be COMPLETED.
        if (!isStaffOrAdmin(roles) && tenantId != null) {
            Optional<StudentProfile> profileOpt = studentProfileRepository.findByTenantIdAndUserId(tenantId, learnerUserId);
            if (profileOpt.isEmpty()) {
                log.warn("Learner {} denied checkpoint: No student profile found for tenant {}", learnerUserId, tenantId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Student academic onboarding must be completed before recording lesson progress.");
            }
            if (profileOpt.get().getOnboardingStatus() != OnboardingStatus.COMPLETED) {
                log.warn("Learner {} denied checkpoint: Academic onboarding is incomplete (status={})",
                        learnerUserId, profileOpt.get().getOnboardingStatus());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Student academic onboarding must be completed before recording lesson progress.");
            }
        }

        LearnerEntitlementService.LessonAndEntitlement le = entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId);
        Lesson lesson = le.lesson();
        Course course = le.course();
        Enrollment enrollment = le.enrollment();

        int durationSeconds = lesson.getDurationMinutes() * 60;
        Optional<LessonMediaAsset> mediaOpt = mediaAssetRepository.findFirstByLessonId(lessonId);
        if (mediaOpt.isPresent() && mediaOpt.get().getDurationSeconds() > 0) {
            durationSeconds = mediaOpt.get().getDurationSeconds();
        }

        // Validate playhead position boundary against duration
        if (durationSeconds > 0 && request.getPlaybackPositionSeconds() > durationSeconds + 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playback position cannot exceed lesson duration.");
        }

        // Session binding & server-side elapsed time limits
        if (request.getSessionToken() != null && !request.getSessionToken().isBlank()) {
            PlaybackSession session = playbackSessionRepository.findBySessionToken(request.getSessionToken())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid playback session token."));

            if (session.isExpired()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Playback session token has expired.");
            }
            if (!session.getLearnerUserId().equals(learnerUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Playback session does not belong to the authenticated learner.");
            }
            if (!session.getLessonId().equals(lessonId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Playback session does not match requested lesson.");
            }
            if (session.getEnrollmentId() != null && enrollment.getId() != null && !session.getEnrollmentId().equals(enrollment.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Playback session does not match active enrollment.");
            }

            // Server-side elapsed time check:
            // Ensure client does not fabricate a delta far larger than actual server-measured elapsed time.
            Instant now = Instant.now();
            Instant referenceTime = session.getLastHeartbeatAt() != null ? session.getLastHeartbeatAt() : session.getCreatedAt();
            long elapsedServerSeconds = Math.max(1, Duration.between(referenceTime, now).getSeconds());

            // Allow up to 2x playback speed plus network latency buffer (15 seconds)
            long maxPermissibleDelta = (elapsedServerSeconds * 2) + 15;
            if (request.getWatchTimeDeltaSeconds() > maxPermissibleDelta) {
                log.warn("Fabricated watch time delta rejected for user {} on lesson {}: claimed {}s, server elapsed {}s (max permissible: {}s)",
                        learnerUserId, lessonId, request.getWatchTimeDeltaSeconds(), elapsedServerSeconds, maxPermissibleDelta);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unrealistic watch time delta: claimed delta (" + request.getWatchTimeDeltaSeconds() +
                        "s) exceeds server-verified elapsed session time (" + elapsedServerSeconds + "s).");
            }

            session.setLastHeartbeatAt(now);
            playbackSessionRepository.save(session);
        }

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

        // Completion policy: strictly governed by meaningful accumulated watch time (>= 80% duration).
        // Forward scrubbing/seeking playhead beyond 90% does NOT qualify for completion.
        boolean shouldComplete = lp.getWatchTimeSeconds() >= (durationSeconds * 0.80);

        if (shouldComplete && !lp.isCompleted()) {
            lp.setCompleted(true);
            lp.setStatus("COMPLETED");
            lp.setCompletionPercentage(100);
            lp.setCompletedAt(Instant.now());
        } else if (!lp.isCompleted()) {
            // Completion percentage reflects meaningful watched time, capped at 99 until completion threshold
            int calculated = (int) ((lp.getWatchTimeSeconds() * 100L) / Math.max(1, durationSeconds));
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

        // Recalculate course percentage strictly scoped to this enrollment
        long completedCount = lessonProgressRepository.countByLearnerUserIdAndCourseIdAndEnrollmentIdAndIsCompletedTrue(learnerUserId, course.getId(), enrollment.getId());
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
                "Progress checkpoint recorded successfully."
        );
    }

    @Transactional
    public PlaybackCheckpointResponse markLessonComplete(UUID learnerUserId, UUID lessonId) {
        return markLessonComplete(learnerUserId, lessonId, null, Collections.emptyList());
    }

    @Transactional
    public PlaybackCheckpointResponse markLessonComplete(UUID learnerUserId, UUID lessonId, UUID tenantId) {
        return markLessonComplete(learnerUserId, lessonId, tenantId, Collections.emptyList());
    }

    @Transactional
    public PlaybackCheckpointResponse markLessonComplete(UUID learnerUserId, UUID lessonId, UUID tenantId, Collection<String> roles) {
        boolean isAdminOrInstructor = isStaffOrAdmin(roles);

        // Student academic onboarding enforcement:
        // Staff/instructors/administrators are exempted when previewing/testing content.
        // For student learners, onboarding profile MUST exist and be COMPLETED.
        if (!isAdminOrInstructor && tenantId != null) {
            Optional<StudentProfile> profileOpt = studentProfileRepository.findByTenantIdAndUserId(tenantId, learnerUserId);
            if (profileOpt.isEmpty()) {
                log.warn("Learner {} denied mark lesson complete: No student profile found for tenant {}", learnerUserId, tenantId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Student academic onboarding must be completed before marking lessons complete.");
            }
            if (profileOpt.get().getOnboardingStatus() != OnboardingStatus.COMPLETED) {
                log.warn("Learner {} denied mark lesson complete: Academic onboarding is incomplete (status={})",
                        learnerUserId, profileOpt.get().getOnboardingStatus());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Student academic onboarding must be completed before marking lessons complete.");
            }
        }

        LearnerEntitlementService.LessonAndEntitlement le = entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId);
        Lesson lesson = le.lesson();
        Course course = le.course();
        Enrollment enrollment = le.enrollment();

        Optional<LessonMediaAsset> mediaOpt = mediaAssetRepository.findFirstByLessonId(lessonId);
        boolean isVideoLesson = mediaOpt.isPresent() || "LECTURE".equalsIgnoreCase(lesson.getLessonType());

        int durationSeconds = lesson.getDurationMinutes() * 60;
        if (mediaOpt.isPresent() && mediaOpt.get().getDurationSeconds() > 0) {
            durationSeconds = mediaOpt.get().getDurationSeconds();
        }

        LearnerLessonProgress lp = lessonProgressRepository
                .findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId())
                .orElseGet(() -> {
                    LearnerLessonProgress created = new LearnerLessonProgress(learnerUserId, enrollment.getId(), course.getId(), lessonId);
                    created.setTenantId(enrollment.getTenantId());
                    created.setCurriculumVersionId(lesson.getModule().getVersion().getId());
                    return created;
                });

        // Enforce completion policies:
        // Video lessons require verified viewing progress (>= 80% duration) unless bypassed by admin/instructor workflow
        if (isVideoLesson && !isAdminOrInstructor) {
            int requiredSeconds = (int) (durationSeconds * 0.80);
            if (lp.getWatchTimeSeconds() < requiredSeconds) {
                log.warn("Unauthorized video completion attempt for user {} on lesson {}: {}s watched out of {}s required",
                        learnerUserId, lessonId, lp.getWatchTimeSeconds(), requiredSeconds);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Video lesson completion requires at least 80% validated watch time (" +
                        lp.getWatchTimeSeconds() + "s watched, " + requiredSeconds + "s required). " +
                        "Viewing threshold cannot be bypassed.");
            }
        }

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
        long completedCount = lessonProgressRepository.countByLearnerUserIdAndCourseIdAndEnrollmentIdAndIsCompletedTrue(learnerUserId, course.getId(), enrollment.getId());
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
