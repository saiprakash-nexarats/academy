package com.nexarats.academy.learning.repository;

import com.nexarats.academy.learning.model.LearnerCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearnerCourseProgressRepository extends JpaRepository<LearnerCourseProgress, UUID> {

    Optional<LearnerCourseProgress> findByLearnerUserIdAndCourseIdAndEnrollmentId(UUID learnerUserId, UUID courseId, UUID enrollmentId);

    Optional<LearnerCourseProgress> findFirstByLearnerUserIdAndCourseIdOrderByLastActivityAtDesc(UUID learnerUserId, UUID courseId);

    List<LearnerCourseProgress> findByLearnerUserIdOrderByLastActivityAtDesc(UUID learnerUserId);
}
