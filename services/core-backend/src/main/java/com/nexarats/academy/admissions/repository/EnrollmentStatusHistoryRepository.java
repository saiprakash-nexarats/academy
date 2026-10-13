package com.nexarats.academy.admissions.repository;

import com.nexarats.academy.admissions.model.EnrollmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentStatusHistoryRepository extends JpaRepository<EnrollmentStatusHistory, UUID> {

    List<EnrollmentStatusHistory> findByEnrollmentIdOrderByCreatedAtAsc(UUID enrollmentId);
}
