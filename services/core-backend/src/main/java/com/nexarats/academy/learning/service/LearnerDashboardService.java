package com.nexarats.academy.learning.service;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.model.Program;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.academic.repository.ProgramRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.repository.CohortRepository;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.learning.dto.LearnerDashboardResponse;
import com.nexarats.academy.learning.model.LearnerCourseProgress;
import com.nexarats.academy.learning.model.LearnerLessonProgress;
import com.nexarats.academy.learning.repository.LearnerCourseProgressRepository;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class LearnerDashboardService {

    private static final Logger log = LoggerFactory.getLogger(LearnerDashboardService.class);

    private final LearnerEntitlementService entitlementService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ProgramRepository programRepository;
    private final CohortRepository cohortRepository;
    private final LessonRepository lessonRepository;
    private final LearnerCourseProgressRepository courseProgressRepository;
    private final LearnerLessonProgressRepository lessonProgressRepository;

    public LearnerDashboardService(LearnerEntitlementService entitlementService,
                                   UserRepository userRepository,
                                   CourseRepository courseRepository,
                                   ProgramRepository programRepository,
                                   CohortRepository cohortRepository,
                                   LessonRepository lessonRepository,
                                   LearnerCourseProgressRepository courseProgressRepository,
                                   LearnerLessonProgressRepository lessonProgressRepository) {
        this.entitlementService = entitlementService;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.programRepository = programRepository;
        this.cohortRepository = cohortRepository;
        this.lessonRepository = lessonRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    @Transactional(readOnly = true)
    public LearnerDashboardResponse getDashboard(UUID learnerUserId) {
        return getDashboard(learnerUserId, null);
    }

    @Transactional(readOnly = true)
    public LearnerDashboardResponse getDashboard(UUID learnerUserId, UUID tenantId) {
        if (learnerUserId == null || tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User and tenant authentication context are required to access dashboard.");
        }

        User user = userRepository.findById(learnerUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learner user not found."));

        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") +
                (user.getLastName() != null ? " " + user.getLastName() : "");
        if (fullName.isBlank()) {
            fullName = user.getEmail();
        }

        List<Enrollment> enrollments = entitlementService.getActiveEnrollments(learnerUserId, tenantId);

        // Map courses and programs
        Map<UUID, Enrollment> courseEnrollmentMap = new LinkedHashMap<>();
        Map<UUID, Enrollment> programEnrollmentMap = new LinkedHashMap<>();

        for (Enrollment e : enrollments) {
            if (e.getCourseId() != null) {
                courseEnrollmentMap.putIfAbsent(e.getCourseId(), e);
            }
            if (e.getProgramId() != null) {
                programEnrollmentMap.putIfAbsent(e.getProgramId(), e);
                // Also grant access to program courses
                List<Course> programCourses = courseRepository.findPublishedCoursesByProgramId(e.getProgramId());
                for (Course pc : programCourses) {
                    courseEnrollmentMap.putIfAbsent(pc.getId(), e);
                }
            }
        }

        List<LearnerDashboardResponse.EnrolledCourseSummary> courseSummaries = new ArrayList<>();
        LearnerDashboardResponse.ContinueLearningTarget continueTarget = null;
        Instant latestActivity = Instant.EPOCH;
        int completedCoursesCount = 0;

        for (Map.Entry<UUID, Enrollment> entry : courseEnrollmentMap.entrySet()) {
            UUID courseId = entry.getKey();
            Enrollment enrollment = entry.getValue();

            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isEmpty()) continue;
            Course course = courseOpt.get();

            Optional<LearnerCourseProgress> progressOpt = courseProgressRepository
                    .findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId());

            int totalLessons = 0;
            CurriculumVersion version = course.getPublishedVersion();
            if (version != null) {
                for (CurriculumModule mod : version.getModules()) {
                    totalLessons += mod.getLessons().size();
                }
            }

            long completedLessons = lessonProgressRepository
                    .countByLearnerUserIdAndCourseIdAndEnrollmentIdAndIsCompletedTrue(learnerUserId, courseId, enrollment.getId());

            int pct = progressOpt.map(LearnerCourseProgress::getCompletionPercentage).orElse(0);
            if (totalLessons > 0) {
                pct = (int) ((completedLessons * 100L) / totalLessons);
            }
            if (pct >= 100) {
                completedCoursesCount++;
            }

            String currentLessonTitle = null;
            UUID currentLessonId = progressOpt.map(LearnerCourseProgress::getCurrentLessonId).orElse(null);
            if (currentLessonId != null) {
                currentLessonTitle = lessonRepository.findById(currentLessonId)
                        .map(Lesson::getLessonTitle)
                        .orElse(null);
            }

            Instant courseLastActivity = progressOpt.map(LearnerCourseProgress::getLastActivityAt).orElse(enrollment.getEnrolledAt());

            LearnerDashboardResponse.EnrolledCourseSummary summary = new LearnerDashboardResponse.EnrolledCourseSummary();
            summary.setCourseId(course.getId());
            summary.setSlug(course.getSlug());
            summary.setTitle(course.getTitle());
            summary.setCategory(course.getCategory());
            summary.setLevel(course.getLevel());
            summary.setImageAssetKey(course.getImageAssetKey());
            summary.setInstructorName(course.getInstructorName());
            summary.setInstructorCompany(course.getInstructorCompany());
            summary.setEnrollmentNumber(enrollment.getEnrollmentNumber());
            summary.setEnrollmentStatus(enrollment.getStatus().name());
            summary.setCompletionPercentage(pct);
            summary.setCompletedLessonsCount((int) completedLessons);
            summary.setTotalLessonsCount(totalLessons);
            summary.setCurrentLessonId(currentLessonId);
            summary.setCurrentLessonTitle(currentLessonTitle);
            summary.setLastActivityAt(courseLastActivity);
            courseSummaries.add(summary);

            // Check if this course should be the continue learning target
            if (courseLastActivity.isAfter(latestActivity) || continueTarget == null) {
                latestActivity = courseLastActivity;
                Lesson targetLesson = null;
                if (currentLessonId != null) {
                    targetLesson = lessonRepository.findById(currentLessonId).orElse(null);
                }
                if (targetLesson == null && version != null && !version.getModules().isEmpty()) {
                    for (CurriculumModule mod : version.getModules()) {
                        if (!mod.getLessons().isEmpty()) {
                            targetLesson = mod.getLessons().get(0);
                            break;
                        }
                    }
                }
                if (targetLesson != null) {
                    Optional<LearnerLessonProgress> lpOpt = lessonProgressRepository
                            .findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, targetLesson.getId(), enrollment.getId());
                    int resumePos = lpOpt.map(LearnerLessonProgress::getLastPlaybackPositionSeconds).orElse(0);

                    continueTarget = new LearnerDashboardResponse.ContinueLearningTarget(
                            course.getId(),
                            course.getSlug(),
                            course.getTitle(),
                            targetLesson.getId(),
                            targetLesson.getLessonTitle(),
                            resumePos,
                            targetLesson.getDurationMinutes()
                    );
                }
            }
        }

        // Program summaries
        List<LearnerDashboardResponse.EnrolledProgramSummary> programSummaries = new ArrayList<>();
        for (Map.Entry<UUID, Enrollment> entry : programEnrollmentMap.entrySet()) {
            UUID progId = entry.getKey();
            Enrollment enrollment = entry.getValue();

            Optional<Program> progOpt = programRepository.findById(progId);
            if (progOpt.isEmpty()) continue;
            Program prog = progOpt.get();

            List<Course> pCourses = courseRepository.findPublishedCoursesByProgramId(progId);
            int totalProgCourses = pCourses.size();
            int completedProgCourses = 0;
            int totalPctSum = 0;

            for (Course pc : pCourses) {
                int cPct = 0;
                long compLessons = lessonProgressRepository.countByLearnerUserIdAndCourseIdAndIsCompletedTrue(learnerUserId, pc.getId());
                int totLessons = 0;
                if (pc.getPublishedVersion() != null) {
                    for (CurriculumModule m : pc.getPublishedVersion().getModules()) {
                        totLessons += m.getLessons().size();
                    }
                }
                if (totLessons > 0) {
                    cPct = (int) ((compLessons * 100L) / totLessons);
                }
                if (cPct >= 100) {
                    completedProgCourses++;
                }
                totalPctSum += cPct;
            }

            int avgProgPct = totalProgCourses > 0 ? (totalPctSum / totalProgCourses) : 0;
            String cohortCode = cohortRepository.findById(enrollment.getCohortId()).map(Cohort::getCohortCode).orElse(null);

            LearnerDashboardResponse.EnrolledProgramSummary ps = new LearnerDashboardResponse.EnrolledProgramSummary();
            ps.setProgramId(prog.getId());
            ps.setSlug(prog.getSlug());
            ps.setTitle(prog.getTitle());
            ps.setDuration(prog.getDurationLabel() != null ? prog.getDurationLabel() : (prog.getDurationValue() + " " + prog.getDurationUnit()));
            ps.setEnrollmentNumber(enrollment.getEnrollmentNumber());
            ps.setCohortCode(cohortCode);
            ps.setCompletionPercentage(avgProgPct);
            ps.setTotalCoursesCount(totalProgCourses);
            ps.setCompletedCoursesCount(completedProgCourses);
            programSummaries.add(ps);
        }

        // Calculate total watch time across enrolled lessons
        List<LearnerLessonProgress> allLessonsProgress = lessonProgressRepository.findByLearnerUserId(learnerUserId);
        int totalWatchSeconds = 0;
        for (LearnerLessonProgress lp : allLessonsProgress) {
            totalWatchSeconds += lp.getWatchTimeSeconds();
        }

        return new LearnerDashboardResponse(
                learnerUserId,
                fullName,
                user.getEmail(),
                courseSummaries.size(),
                completedCoursesCount,
                totalWatchSeconds / 60,
                continueTarget,
                courseSummaries,
                programSummaries
        );
    }
}
