package com.nexarats.academy.academic.repository;

import com.nexarats.academy.academic.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleIdOrderByDisplayOrderAsc(UUID moduleId);

    Optional<Lesson> findByIdAndIsPreviewableTrue(UUID lessonId);
}
