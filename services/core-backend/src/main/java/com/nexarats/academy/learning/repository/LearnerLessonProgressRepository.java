package com.nexarats.academy.learning.repository;

import com.nexarats.academy.learning.model.LearnerLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearnerLessonProgressRepository extends JpaRepository<LearnerLessonProgress, UUID> {

    Optional<LearnerLessonProgress> findByLearnerUserIdAndLessonIdAndEnrollmentId(UUID learnerUserId, UUID lessonId, UUID enrollmentId);

    Optional<LearnerLessonProgress> findFirstByLearnerUserIdAndLessonIdOrderByLastActivityAtDesc(UUID learnerUserId, UUID lessonId);

    List<LearnerLessonProgress> findByLearnerUserId(UUID learnerUserId);

    List<LearnerLessonProgress> findByLearnerUserIdAndCourseId(UUID learnerUserId, UUID courseId);

    long countByLearnerUserIdAndCourseIdAndIsCompletedTrue(UUID learnerUserId, UUID courseId);
}
