package com.nexarats.academy.admissions.repository;

import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findByEnrollmentNumber(String enrollmentNumber);

    List<Enrollment> findByLearnerUserIdOrderByEnrolledAtDesc(UUID learnerUserId);

    Optional<Enrollment> findByLearnerUserIdAndCohortId(UUID learnerUserId, UUID cohortId);

    boolean existsByLearnerUserIdAndCohortIdAndStatusIn(UUID learnerUserId, UUID cohortId, List<EnrollmentStatus> statuses);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.cohortId = :cohortId AND e.status IN ('ENROLLED', 'ACTIVE', 'COMPLETED')")
    long countConfirmedEnrollmentsByCohortId(@Param("cohortId") UUID cohortId);

    @Query("SELECT e FROM Enrollment e WHERE (:tenantId IS NULL OR e.tenantId = :tenantId OR e.tenantId IS NULL) " +
            "AND (:cohortId IS NULL OR e.cohortId = :cohortId) " +
            "AND (:status IS NULL OR e.status = :status) " +
            "ORDER BY e.enrolledAt DESC")
    Page<Enrollment> findEnrollmentsWithFilter(
            @Param("tenantId") UUID tenantId,
            @Param("cohortId") UUID cohortId,
            @Param("status") EnrollmentStatus status,
            Pageable pageable);
}
