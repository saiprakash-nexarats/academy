package com.nexarats.academy.admissions.service;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.Program;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.ProgramRepository;
import com.nexarats.academy.admissions.model.Application;
import com.nexarats.academy.admissions.model.ApplicationStatus;
import com.nexarats.academy.admissions.model.ApplicationStatusHistory;
import com.nexarats.academy.admissions.repository.ApplicationRepository;
import com.nexarats.academy.admissions.repository.ApplicationStatusHistoryRepository;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.model.CohortStatus;
import com.nexarats.academy.cohort.repository.CohortRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final CohortRepository cohortRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ApplicationStatusHistoryRepository statusHistoryRepository,
                              ProgramRepository programRepository,
                              CourseRepository courseRepository,
                              CohortRepository cohortRepository) {
        this.applicationRepository = applicationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.cohortRepository = cohortRepository;
    }

    @Transactional
    public Application submitApplication(
            String fullName,
            String email,
            String phone,
            String targetProgramOrCourse,
            String experienceLevel,
            String statementOfPurpose,
            UUID authenticatedUserId,
            UUID tenantId) {

        if (fullName == null || fullName.trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name must be at least 2 characters long.");
        }
        if (email == null || !email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid email address is required.");
        }
        if (phone == null || phone.trim().length() < 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid phone number is required.");
        }

        UUID resolvedProgramId = null;
        UUID resolvedCourseId = null;
        UUID resolvedCohortId = null;

        // Resolve target program or course by slug or ID
        if (targetProgramOrCourse != null && !targetProgramOrCourse.isBlank()) {
            Optional<Program> programOpt = programRepository.findBySlug(targetProgramOrCourse);
            if (programOpt.isEmpty() && targetProgramOrCourse.startsWith("full-stack")) {
                programOpt = programRepository.findBySlug("full-stack");
            }

            if (programOpt.isPresent()) {
                resolvedProgramId = programOpt.get().getId();
            } else {
                Optional<Course> courseOpt = courseRepository.findBySlug(targetProgramOrCourse);
                if (courseOpt.isPresent()) {
                    resolvedCourseId = courseOpt.get().getId();
                } else {
                    try {
                        UUID uuid = UUID.fromString(targetProgramOrCourse);
                        if (programRepository.existsById(uuid)) {
                            resolvedProgramId = uuid;
                        } else if (courseRepository.existsById(uuid)) {
                            resolvedCourseId = uuid;
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        // Find active cohort for target
        if (resolvedProgramId != null) {
            List<Cohort> cohorts = cohortRepository.findByProgramId(resolvedProgramId);
            resolvedCohortId = cohorts.stream()
                    .filter(c -> c.getStatus() == CohortStatus.OPEN_FOR_ENROLLMENT)
                    .findFirst()
                    .map(Cohort::getId)
                    .orElse(null);
        } else if (resolvedCourseId != null) {
            List<Cohort> cohorts = cohortRepository.findByCourseId(resolvedCourseId);
            resolvedCohortId = cohorts.stream()
                    .filter(c -> c.getStatus() == CohortStatus.OPEN_FOR_ENROLLMENT)
                    .findFirst()
                    .map(Cohort::getId)
                    .orElse(null);
        }

        // Duplicate check
        List<ApplicationStatus> activeStatuses = List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.APPROVED);
        if (authenticatedUserId != null) {
            if (resolvedProgramId != null && applicationRepository.existsByApplicantUserIdAndProgramIdAndStatusIn(authenticatedUserId, resolvedProgramId, activeStatuses)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active application for this career program.");
            }
            if (resolvedCourseId != null && applicationRepository.existsByApplicantUserIdAndCourseIdAndStatusIn(authenticatedUserId, resolvedCourseId, activeStatuses)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active application for this course.");
            }
            if (resolvedProgramId == null && resolvedCourseId == null && applicationRepository.existsByApplicantUserIdAndStatusIn(authenticatedUserId, activeStatuses)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active application under review.");
            }
        } else {
            if (resolvedProgramId != null && applicationRepository.existsByEmailAndProgramIdAndStatusIn(email, resolvedProgramId, activeStatuses)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An active application with this email already exists for this program.");
            }
            if (resolvedCourseId != null && applicationRepository.existsByEmailAndCourseIdAndStatusIn(email, resolvedCourseId, activeStatuses)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An active application with this email already exists for this course.");
            }
            if (resolvedProgramId == null && resolvedCourseId == null && applicationRepository.existsByEmailAndStatusIn(email, activeStatuses)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "An active application with this email already exists.");
            }
        }

        String appNumber = "APP-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Application app = new Application(appNumber, fullName.trim(), email.trim(), phone.trim(), (experienceLevel != null ? experienceLevel : "beginner"));
        app.setStatementOfPurpose(statementOfPurpose);
        app.setApplicantUserId(authenticatedUserId);
        app.setTenantId(tenantId);
        app.setProgramId(resolvedProgramId);
        app.setCourseId(resolvedCourseId);
        app.setCohortId(resolvedCohortId);
        app.setStatus(ApplicationStatus.SUBMITTED);

        Application saved = applicationRepository.save(app);

        // Record history
        ApplicationStatusHistory history = new ApplicationStatusHistory(
                saved.getId(),
                null,
                ApplicationStatus.SUBMITTED,
                authenticatedUserId,
                "Application submitted by applicant."
        );
        statusHistoryRepository.save(history);

        log.info("Application {} submitted successfully for email: {}, target program/course: {}", appNumber, email, targetProgramOrCourse);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Application> getMyApplications(UUID applicantUserId) {
        if (applicantUserId == null) return List.of();
        return applicationRepository.findByApplicantUserIdOrderBySubmittedAtDesc(applicantUserId);
    }

    @Transactional(readOnly = true)
    public Application getApplication(UUID id, UUID currentUserId, boolean isAdmin) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + id));

        if (!isAdmin && (currentUserId == null || !currentUserId.equals(app.getApplicantUserId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You are not authorized to view this application.");
        }
        return app;
    }

    @Transactional
    public Application withdrawApplication(UUID id, UUID currentUserId, String reason) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + id));

        if (currentUserId == null || !currentUserId.equals(app.getApplicantUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Only the applicant can withdraw this application.");
        }

        if (app.getStatus() != ApplicationStatus.SUBMITTED && app.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot withdraw an application that is already " + app.getStatus());
        }

        ApplicationStatus fromStatus = app.getStatus();
        app.setStatus(ApplicationStatus.WITHDRAWN);
        Application updated = applicationRepository.save(app);

        ApplicationStatusHistory history = new ApplicationStatusHistory(
                app.getId(),
                fromStatus,
                ApplicationStatus.WITHDRAWN,
                currentUserId,
                reason != null ? reason : "Withdrawn by applicant."
        );
        statusHistoryRepository.save(history);

        log.info("Application {} withdrawn by user {}", app.getApplicationNumber(), currentUserId);
        return updated;
    }

    @Transactional(readOnly = true)
    public Page<Application> listApplications(UUID tenantId, ApplicationStatus status, UUID programId, UUID courseId, Pageable pageable) {
        return applicationRepository.findApplicationsWithFilter(tenantId, status, programId, courseId, pageable);
    }
}
