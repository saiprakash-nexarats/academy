package com.nexarats.academy.academic.repository;

import com.nexarats.academy.academic.model.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID> {

    Optional<Program> findBySlug(String slug);

    Optional<Program> findBySlugAndStatus(String slug, String status);

    List<Program> findByStatusOrderByDisplayOrderAsc(String status);

    List<Program> findByStatusAndIsFeaturedTrueOrderByDisplayOrderAsc(String status);

    @Query("SELECT p FROM Program p JOIN p.category c " +
           "WHERE p.status = 'PUBLISHED' " +
           "AND ((:tenantId IS NULL AND p.tenantId IS NULL) OR (:tenantId IS NOT NULL AND (p.tenantId IS NULL OR p.tenantId = :tenantId))) " +
           "AND (:category IS NULL OR :category = '' OR :category = 'all' OR LOWER(c.slug) = LOWER(:category) OR LOWER(c.name) = LOWER(:category)) " +
           "AND (:duration IS NULL OR p.durationValue = :duration) " +
           "AND (:featured IS NULL OR p.isFeatured = :featured) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.detailedDescription) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.displayOrder ASC")
    List<Program> findPublishedWithFilters(
            @Param("tenantId") UUID tenantId,
            @Param("category") String category,
            @Param("duration") Integer duration,
            @Param("featured") Boolean featured,
            @Param("search") String search
    );

    @Query("SELECT p FROM Program p JOIN p.category c " +
           "WHERE p.status = 'PUBLISHED' " +
           "AND ((:tenantId IS NULL AND p.tenantId IS NULL) OR (:tenantId IS NOT NULL AND (p.tenantId IS NULL OR p.tenantId = :tenantId))) " +
           "AND (:category IS NULL OR :category = '' OR :category = 'all' OR LOWER(c.slug) = LOWER(:category) OR LOWER(c.name) = LOWER(:category)) " +
           "AND (:duration IS NULL OR p.durationValue = :duration) " +
           "AND (:featured IS NULL OR p.isFeatured = :featured) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.detailedDescription) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Program> findPublishedWithFiltersPaged(
            @Param("tenantId") UUID tenantId,
            @Param("category") String category,
            @Param("duration") Integer duration,
            @Param("featured") Boolean featured,
            @Param("search") String search,
            Pageable pageable
    );
}
