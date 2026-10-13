package com.nexarats.academy.academic.repository;

import com.nexarats.academy.academic.model.CurriculumVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurriculumVersionRepository extends JpaRepository<CurriculumVersion, UUID> {

    List<CurriculumVersion> findByCourseIdOrderByVersionNumberDesc(UUID courseId);

    Optional<CurriculumVersion> findByCourseIdAndVersionNumber(UUID courseId, int versionNumber);

    Optional<CurriculumVersion> findByCourseIdAndStatus(UUID courseId, String status);
}
