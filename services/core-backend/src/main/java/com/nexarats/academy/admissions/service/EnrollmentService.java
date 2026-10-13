package com.nexarats.academy.admissions.service;

import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.model.EnrollmentStatusHistory;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.admissions.repository.EnrollmentStatusHistoryRepository;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.service.CohortSeatService;
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
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentStatusHistoryRepository enrollmentStatusHistoryRepository;
    private final CohortSeatService cohortSeatService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             EnrollmentStatusHistoryRepository enrollmentStatusHistoryRepository,
                             CohortSeatService cohortSeatService) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentStatusHistoryRepository = enrollmentStatusHistoryRepository;
        this.cohortSeatService = cohortSeatService;
    }

    @Transactional
    public Enrollment confirmEnrollment(UUID learnerUserId, UUID cohortId, UUID applicationId, UUID paymentOrderId, UUID reservationId) {
        if (learnerUserId == null || cohortId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Learner ID and Cohort ID are mandatory for enrollment.");
        }

        // Idempotency check: if learner is already enrolled in this cohort, return existing record
        Optional<Enrollment> existingOpt = enrollmentRepository.findByLearnerUserIdAndCohortId(learnerUserId, cohortId);
        if (existingOpt.isPresent()) {
            Enrollment existing = existingOpt.get();
            log.info("Learner {} is already enrolled in cohort {}. Returning existing enrollment {}", learnerUserId, cohortId, existing.getEnrollmentNumber());
            return existing;
        }

        Cohort cohort = cohortSeatService.getCohort(cohortId);

        String enrollmentNum = "ENR-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Enrollment enrollment = new Enrollment(enrollmentNum, learnerUserId, cohortId, EnrollmentStatus.ENROLLED);
        enrollment.setTenantId(cohort.getTenantId());
        enrollment.setProgramId(cohort.getProgramId());
        enrollment.setCourseId(cohort.getCourseId());
        enrollment.setApplicationId(applicationId);
        enrollment.setPaymentOrderId(paymentOrderId);

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Record audit history
        EnrollmentStatusHistory history = new EnrollmentStatusHistory(
                saved.getId(),
                null,
                EnrollmentStatus.ENROLLED,
                learnerUserId,
                "Payment verified and student enrollment confirmed."
        );
        enrollmentStatusHistoryRepository.save(history);

        // Confirm seat reservation or record direct enrollment
        if (reservationId != null) {
            cohortSeatService.confirmReservation(reservationId);
        } else {
            cohortSeatService.recordDirectEnrollment(cohortId);
        }

        log.info("Successfully activated enrollment {} for learner {} in cohort {}", enrollmentNum, learnerUserId, cohort.getCohortCode());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getMyEnrollments(UUID learnerUserId) {
        if (learnerUserId == null) return List.of();
        return enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId);
    }

    @Transactional(readOnly = true)
    public Page<Enrollment> getEnrollments(UUID tenantId, UUID cohortId, EnrollmentStatus status, Pageable pageable) {
        return enrollmentRepository.findEnrollmentsWithFilter(tenantId, cohortId, status, pageable);
    }

    @Transactional(readOnly = true)
    public boolean isLearnerEnrolledInCourse(UUID learnerUserId, UUID courseId) {
        if (learnerUserId == null || courseId == null) return false;
        List<Enrollment> enrollments = enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId);
        return enrollments.stream().anyMatch(e ->
                (e.getCourseId() != null && e.getCourseId().equals(courseId)) &&
                        (e.getStatus() == EnrollmentStatus.ENROLLED || e.getStatus() == EnrollmentStatus.ACTIVE));
    }

    @Transactional(readOnly = true)
    public boolean isLearnerEnrolledInProgram(UUID learnerUserId, UUID programId) {
        if (learnerUserId == null || programId == null) return false;
        List<Enrollment> enrollments = enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId);
        return enrollments.stream().anyMatch(e ->
                (e.getProgramId() != null && e.getProgramId().equals(programId)) &&
                        (e.getStatus() == EnrollmentStatus.ENROLLED || e.getStatus() == EnrollmentStatus.ACTIVE));
    }
}
