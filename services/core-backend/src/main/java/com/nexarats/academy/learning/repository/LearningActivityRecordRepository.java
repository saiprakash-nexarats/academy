package com.nexarats.academy.learning.repository;

import com.nexarats.academy.learning.model.LearningActivityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningActivityRecordRepository extends JpaRepository<LearningActivityRecord, UUID> {

    Optional<LearningActivityRecord> findByIdempotencyKey(String idempotencyKey);

    List<LearningActivityRecord> findByLearnerUserIdAndCourseIdOrderByCreatedAtDesc(UUID learnerUserId, UUID courseId);
}
