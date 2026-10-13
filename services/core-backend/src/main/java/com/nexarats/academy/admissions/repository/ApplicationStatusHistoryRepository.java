package com.nexarats.academy.admissions.repository;

import com.nexarats.academy.admissions.model.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, UUID> {

    List<ApplicationStatusHistory> findByApplicationIdOrderByCreatedAtAsc(UUID applicationId);
}
