package com.nexarats.academy.admissions.repository;

import com.nexarats.academy.admissions.model.Application;
import com.nexarats.academy.admissions.model.ApplicationStatus;
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
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByApplicationNumber(String applicationNumber);

    List<Application> findByApplicantUserIdOrderBySubmittedAtDesc(UUID applicantUserId);

    List<Application> findByEmailOrderBySubmittedAtDesc(String email);

    boolean existsByApplicantUserIdAndProgramIdAndStatusIn(UUID applicantUserId, UUID programId, List<ApplicationStatus> statuses);

    boolean existsByApplicantUserIdAndCourseIdAndStatusIn(UUID applicantUserId, UUID courseId, List<ApplicationStatus> statuses);

    boolean existsByApplicantUserIdAndStatusIn(UUID applicantUserId, List<ApplicationStatus> statuses);

    boolean existsByEmailAndProgramIdAndStatusIn(String email, UUID programId, List<ApplicationStatus> statuses);

    boolean existsByEmailAndCourseIdAndStatusIn(String email, UUID courseId, List<ApplicationStatus> statuses);

    boolean existsByEmailAndStatusIn(String email, List<ApplicationStatus> statuses);

    @Query("SELECT a FROM Application a WHERE (:tenantId IS NULL OR a.tenantId = :tenantId OR a.tenantId IS NULL) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:programId IS NULL OR a.programId = :programId) " +
            "AND (:courseId IS NULL OR a.courseId = :courseId) " +
            "ORDER BY a.submittedAt DESC")
    Page<Application> findApplicationsWithFilter(
            @Param("tenantId") UUID tenantId,
            @Param("status") ApplicationStatus status,
            @Param("programId") UUID programId,
            @Param("courseId") UUID courseId,
            Pageable pageable);
}
