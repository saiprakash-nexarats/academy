package com.nexarats.academy.academic.repository;

import com.nexarats.academy.academic.model.CurriculumModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CurriculumModuleRepository extends JpaRepository<CurriculumModule, UUID> {

    List<CurriculumModule> findByVersionIdOrderByDisplayOrderAsc(UUID versionId);

    @Query("SELECT DISTINCT m FROM CurriculumModule m LEFT JOIN FETCH m.lessons l " +
           "WHERE m.version.id = :versionId " +
           "ORDER BY m.displayOrder ASC")
    List<CurriculumModule> findByVersionIdWithLessons(@Param("versionId") UUID versionId);

    Optional<CurriculumModule> findByVersionIdAndModuleNumber(UUID versionId, String moduleNumber);
}
