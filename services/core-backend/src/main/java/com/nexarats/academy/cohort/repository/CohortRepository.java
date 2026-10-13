package com.nexarats.academy.cohort.repository;

import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.model.CohortStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CohortRepository extends JpaRepository<Cohort, UUID> {

    Optional<Cohort> findByCohortCode(String cohortCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cohort c WHERE c.id = :id")
    Optional<Cohort> findByIdForUpdate(@Param("id") UUID id);

    List<Cohort> findByProgramId(UUID programId);

    List<Cohort> findByCourseId(UUID courseId);

    List<Cohort> findByStatus(CohortStatus status);

    @Query("SELECT c FROM Cohort c WHERE (c.tenantId IS NULL OR c.tenantId = :tenantId) AND (:status IS NULL OR c.status = :status) ORDER BY c.startDate ASC")
    List<Cohort> findByTenantAndStatus(@Param("tenantId") UUID tenantId, @Param("status") CohortStatus status);
}
