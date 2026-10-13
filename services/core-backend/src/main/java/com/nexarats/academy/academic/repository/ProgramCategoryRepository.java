package com.nexarats.academy.academic.repository;

import com.nexarats.academy.academic.model.ProgramCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramCategoryRepository extends JpaRepository<ProgramCategory, UUID> {

    Optional<ProgramCategory> findBySlug(String slug);

    List<ProgramCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
}
