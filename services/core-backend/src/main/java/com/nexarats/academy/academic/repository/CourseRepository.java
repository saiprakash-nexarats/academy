package com.nexarats.academy.academic.repository;

import com.nexarats.academy.academic.model.Course;
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
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findBySlug(String slug);

    Optional<Course> findBySlugAndStatus(String slug, String status);

    List<Course> findByStatusOrderByDisplayOrderAsc(String status);

    List<Course> findByStatusAndIsFeaturedTrueOrderByDisplayOrderAsc(String status);

    @Query("SELECT c FROM Course c " +
           "WHERE c.status = 'PUBLISHED' " +
           "AND ((:tenantId IS NULL AND c.tenantId IS NULL) OR (:tenantId IS NOT NULL AND (c.tenantId IS NULL OR c.tenantId = :tenantId))) " +
           "AND (:category IS NULL OR :category = '' OR :category = 'all' OR LOWER(c.category) = LOWER(:category)) " +
           "AND (:level IS NULL OR :level = '' OR :level = 'all' OR LOWER(c.level) = LOWER(:level)) " +
           "AND (:featured IS NULL OR c.isFeatured = :featured) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(c.instructorName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(c.instructorCompany) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(c.techStack) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Course> findPublishedWithFiltersPaged(
            @Param("tenantId") UUID tenantId,
            @Param("category") String category,
            @Param("level") String level,
            @Param("featured") Boolean featured,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT c FROM Course c JOIN c.programs p " +
           "WHERE p.id = :programId AND c.status = 'PUBLISHED' " +
           "ORDER BY c.displayOrder ASC")
    List<Course> findPublishedCoursesByProgramId(@Param("programId") UUID programId);
}
