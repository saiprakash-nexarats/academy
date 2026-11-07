package com.nexarats.academy.learning.web;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.CurriculumModuleRepository;
import com.nexarats.academy.academic.repository.CurriculumVersionRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.learning.dto.*;
import com.nexarats.academy.learning.model.LearnerLessonProgress;
import com.nexarats.academy.learning.repository.LearnerCourseProgressRepository;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.service.LearnerDashboardService;
import com.nexarats.academy.learning.service.LearnerEntitlementService;
import com.nexarats.academy.learning.service.LearnerProgressService;
import com.nexarats.academy.learning.service.VideoPlaybackService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/v1/learning")
@PreAuthorize("hasAnyRole('STUDENT', 'EMPLOYEE', 'ACADEMY_ADMIN', 'SUPER_ADMIN')")
public class LearningController {

    private final LearnerDashboardService dashboardService;
    private final LearnerEntitlementService entitlementService;
    private final LearnerProgressService progressService;
    private final VideoPlaybackService playbackService;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CurriculumVersionRepository versionRepository;
    private final CurriculumModuleRepository moduleRepository;
    private final LearnerCourseProgressRepository courseProgressRepository;
    private final LearnerLessonProgressRepository lessonProgressRepository;

    public LearningController(LearnerDashboardService dashboardService,
                              LearnerEntitlementService entitlementService,
                              LearnerProgressService progressService,
                              VideoPlaybackService playbackService,
                              CourseRepository courseRepository,
                              LessonRepository lessonRepository,
                              CurriculumVersionRepository versionRepository,
                              CurriculumModuleRepository moduleRepository,
                              LearnerCourseProgressRepository courseProgressRepository,
                              LearnerLessonProgressRepository lessonProgressRepository) {
        this.dashboardService = dashboardService;
        this.entitlementService = entitlementService;
        this.progressService = progressService;
        this.playbackService = playbackService;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.versionRepository = versionRepository;
        this.moduleRepository = moduleRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<LearnerDashboardResponse> getDashboard(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        LearnerDashboardResponse response = dashboardService.getDashboard(principal.getUserId(), principal.getTenantId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<LearnerDashboardResponse.EnrolledCourseSummary>> getMyCourses(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        LearnerDashboardResponse dashboard = dashboardService.getDashboard(principal.getUserId(), principal.getTenantId());
        return ResponseEntity.ok(dashboard.getEnrolledCourses());
    }

    @GetMapping("/my-programs")
    public ResponseEntity<List<LearnerDashboardResponse.EnrolledProgramSummary>> getMyPrograms(@AuthenticationPrincipal UserPrincipal principal) {
        assertAuthenticated(principal);
        LearnerDashboardResponse dashboard = dashboardService.getDashboard(principal.getUserId(), principal.getTenantId());
        return ResponseEntity.ok(dashboard.getEnrolledPrograms());
    }

    @GetMapping("/courses/{courseId}/syllabus")
    @Transactional(readOnly = true)
    public ResponseEntity<LearnerSyllabusResponse> getCourseSyllabus(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);
        Enrollment enrollment = entitlementService.verifyCourseEntitlement(principal.getUserId(), courseId, principal.getTenantId());
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found: " + courseId));

        CurriculumVersion version = versionRepository.findByCourseIdAndStatus(courseId, "PUBLISHED")
                .orElseGet(() -> {
                    if (course.getPublishedVersionId() != null) {
                        return versionRepository.findById(course.getPublishedVersionId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No published curriculum version found for course."));
                    }
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "No published curriculum version found for course.");
                });

        List<CurriculumModule> modules = moduleRepository.findByVersionIdWithLessons(version.getId());

        List<LearnerLessonProgress> existingProgress = lessonProgressRepository
                .findByLearnerUserIdAndCourseIdAndEnrollmentId(principal.getUserId(), courseId, enrollment.getId());
        Map<UUID, LearnerLessonProgress> progressMap = new HashMap<>();
        for (LearnerLessonProgress lp : existingProgress) {
            progressMap.put(lp.getLessonId(), lp);
        }

        List<LearnerSyllabusResponse.LearnerModuleDto> moduleDtos = new ArrayList<>();
        int totalLessons = 0;
        int completedLessons = 0;

        for (CurriculumModule mod : modules) {
            LearnerSyllabusResponse.LearnerModuleDto mDto = new LearnerSyllabusResponse.LearnerModuleDto();
            mDto.setModuleId(mod.getId());
            mDto.setModuleNumber(mod.getModuleNumber());
            mDto.setTitle(mod.getTitle());
            mDto.setDurationLabel(mod.getDurationLabel());
            mDto.setDisplayOrder(mod.getDisplayOrder());

            List<LearnerSyllabusResponse.LearnerLessonDto> lDtos = new ArrayList<>();
            List<Lesson> lessons = mod.getLessons() != null ? mod.getLessons() : Collections.emptyList();
            for (Lesson l : lessons) {
                totalLessons++;
                LearnerLessonProgress lp = progressMap.get(l.getId());
                boolean isCompleted = lp != null && lp.isCompleted();
                if (isCompleted) {
                    completedLessons++;
                }

                LearnerSyllabusResponse.LearnerLessonDto lDto = new LearnerSyllabusResponse.LearnerLessonDto();
                lDto.setLessonId(l.getId());
                lDto.setLessonTitle(l.getLessonTitle());
                lDto.setSummary(l.getSummary());
                lDto.setLessonType(l.getLessonType());
                lDto.setDurationMinutes(l.getDurationMinutes());
                lDto.setDisplayOrder(l.getDisplayOrder());
                lDto.setPreviewable(l.isPreviewable());
                lDto.setLocked(false);
                lDto.setCompleted(isCompleted);
                lDto.setLastPlaybackPositionSeconds(lp != null ? lp.getLastPlaybackPositionSeconds() : 0);
                lDto.setWatchTimeSeconds(lp != null ? lp.getWatchTimeSeconds() : 0);
                lDtos.add(lDto);
            }
            mDto.setLessons(lDtos);
            moduleDtos.add(mDto);
        }

        int overallPercentage = totalLessons > 0 ? (int) ((completedLessons * 100L) / totalLessons) : 0;
        LearnerSyllabusResponse resp = new LearnerSyllabusResponse(
                course.getId(),
                course.getTitle(),
                version.getVersionLabel(),
                totalLessons,
                completedLessons,
                overallPercentage,
                moduleDtos
        );

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity<LessonDetailResponse> getLessonDetail(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);
        LearnerEntitlementService.LessonAndEntitlement le = entitlementService.verifyLessonEntitlement(principal.getUserId(), lessonId, principal.getTenantId());
        Lesson lesson = le.lesson();
        Course course = le.course();
        Enrollment enrollment = le.enrollment();

        if (!course.getId().equals(courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson does not belong to specified course.");
        }

        CurriculumModule module = lesson.getModule();
        CurriculumVersion version = module.getVersion();

        // Sequential ordering: find previous and next lessons
        List<Lesson> orderedLessons = new ArrayList<>();
        for (CurriculumModule mod : version.getModules()) {
            orderedLessons.addAll(mod.getLessons());
        }

        UUID prevId = null;
        UUID nextId = null;
        for (int i = 0; i < orderedLessons.size(); i++) {
            if (orderedLessons.get(i).getId().equals(lessonId)) {
                if (i > 0) prevId = orderedLessons.get(i - 1).getId();
                if (i < orderedLessons.size() - 1) nextId = orderedLessons.get(i + 1).getId();
                break;
            }
        }

        Optional<LearnerLessonProgress> lpOpt = lessonProgressRepository
                .findByLearnerUserIdAndLessonIdAndEnrollmentId(principal.getUserId(), lessonId, enrollment.getId());

        LessonDetailResponse resp = new LessonDetailResponse();
        resp.setLessonId(lesson.getId());
        resp.setCourseId(course.getId());
        resp.setCourseTitle(course.getTitle());
        resp.setModuleId(module.getId());
        resp.setModuleTitle(module.getTitle());
        resp.setLessonTitle(lesson.getLessonTitle());
        resp.setSummary(lesson.getSummary());
        resp.setLessonType(lesson.getLessonType());
        resp.setDurationMinutes(lesson.getDurationMinutes());
        resp.setPreviewable(lesson.isPreviewable());
        resp.setLocked(false);
        resp.setCompleted(lpOpt.map(LearnerLessonProgress::isCompleted).orElse(false));
        resp.setLastPlaybackPositionSeconds(lpOpt.map(LearnerLessonProgress::getLastPlaybackPositionSeconds).orElse(0));
        resp.setWatchTimeSeconds(lpOpt.map(LearnerLessonProgress::getWatchTimeSeconds).orElse(0));
        resp.setContentBody(lesson.getPreviewContent() != null ? lesson.getPreviewContent() : lesson.getSummary());
        resp.setPreviousLessonId(prevId);
        resp.setNextLessonId(nextId);
        resp.setHasMediaStream(true);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);
        CourseProgressResponse resp = progressService.getCourseProgress(principal.getUserId(), courseId, principal.getTenantId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/lessons/{lessonId}/playback-session")
    public ResponseEntity<PlaybackSessionResponse> createPlaybackSession(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);
        PlaybackSessionResponse resp = playbackService.createPlaybackSession(
                principal.getUserId(), lessonId, principal.getTenantId(), principal.getRoles()
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<PlaybackCheckpointResponse> recordProgressCheckpoint(
            @PathVariable UUID lessonId,
            @Valid @RequestBody PlaybackCheckpointRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);
        PlaybackCheckpointResponse resp = progressService.recordCheckpoint(
                principal.getUserId(), lessonId, request, principal.getTenantId(), principal.getRoles()
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<PlaybackCheckpointResponse> markLessonComplete(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        assertAuthenticated(principal);
        PlaybackCheckpointResponse resp = progressService.markLessonComplete(principal.getUserId(), lessonId, principal.getTenantId(), principal.getRoles());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/media/stream")
    @PreAuthorize("permitAll()")
    public void streamMedia(
            @RequestParam("token") String token,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletResponse response
    ) {
        playbackService.streamMedia(token, rangeHeader, response);
    }

    private void assertAuthenticated(UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null || principal.getTenantId() == null) {
            throw new AccessDeniedException("Authentication and tenant context required to access learning resources.");
        }
    }
}
