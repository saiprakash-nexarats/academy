package com.nexarats.academy.learning.service;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LearnerEntitlementService {

    private static final Logger log = LoggerFactory.getLogger(LearnerEntitlementService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public LearnerEntitlementService(EnrollmentRepository enrollmentRepository,
                                     CourseRepository courseRepository,
                                     LessonRepository lessonRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getActiveEnrollments(UUID learnerUserId) {
        if (learnerUserId == null) return List.of();
        List<Enrollment> all = enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId);
        return all.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED || e.getStatus() == EnrollmentStatus.ACTIVE)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasCourseEntitlement(UUID learnerUserId, UUID courseId) {
        if (learnerUserId == null || courseId == null) return false;
        List<Enrollment> active = getActiveEnrollments(learnerUserId);

        // 1. Direct course enrollment
        for (Enrollment e : active) {
            if (e.getCourseId() != null && e.getCourseId().equals(courseId)) {
                return true;
            }
        }

        // 2. Program-level course entitlement
        for (Enrollment e : active) {
            if (e.getProgramId() != null) {
                List<Course> programCourses = courseRepository.findPublishedCoursesByProgramId(e.getProgramId());
                boolean matches = programCourses.stream().anyMatch(c -> c.getId().equals(courseId));
                if (matches) {
                    return true;
                }
            }
        }

        return false;
    }

    @Transactional(readOnly = true)
    public Enrollment verifyCourseEntitlement(UUID learnerUserId, UUID courseId) {
        if (learnerUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User must be authenticated to access course content.");
        }
        if (courseId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course ID cannot be null.");
        }

        List<Enrollment> active = getActiveEnrollments(learnerUserId);

        // 1. Direct course match
        for (Enrollment e : active) {
            if (e.getCourseId() != null && e.getCourseId().equals(courseId)) {
                return e;
            }
        }

        // 2. Program-level match
        for (Enrollment e : active) {
            if (e.getProgramId() != null) {
                List<Course> programCourses = courseRepository.findPublishedCoursesByProgramId(e.getProgramId());
                boolean matches = programCourses.stream().anyMatch(c -> c.getId().equals(courseId));
                if (matches) {
                    return e;
                }
            }
        }

        log.warn("Learner {} denied access to course {}: No active enrollment found.", learnerUserId, courseId);
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Active enrollment required to access course content.");
    }

    @Transactional(readOnly = true)
    public LessonAndEntitlement verifyLessonEntitlement(UUID learnerUserId, UUID lessonId) {
        if (lessonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson ID cannot be null.");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found: " + lessonId));

        Course course = lesson.getModule().getVersion().getCourse();
        Enrollment enrollment = verifyCourseEntitlement(learnerUserId, course.getId());

        return new LessonAndEntitlement(lesson, course, enrollment);
    }

    public record LessonAndEntitlement(Lesson lesson, Course course, Enrollment enrollment) {}
}
