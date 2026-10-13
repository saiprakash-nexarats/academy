package com.nexarats.academy.academic.web;

import com.nexarats.academy.academic.dto.*;
import com.nexarats.academy.academic.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public REST Controller for the Nexarats Academy Modular Course Catalog & Syllabus Engine.
 * Supports canonical /api/v1/academic/courses and compatibility alias /api/v1/courses.
 * Publicly accessible, read-only, Redis cached, with secure boundaries for locked lesson content.
 */
@RestController
@RequestMapping({"/api/v1/academic/courses", "/api/v1/courses"})
public class CourseCatalogController {

    private final CourseService courseService;

    public CourseCatalogController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<CourseSummaryDto>> listCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "default") String sort
    ) {
        String searchQuery = (search != null && !search.isBlank()) ? search : q;
        PaginatedResponseDto<CourseSummaryDto> result = courseService.getPublishedCourses(
                category,
                level,
                featured,
                searchQuery,
                page,
                size,
                sort
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CourseDetailDto> getCourseBySlug(@PathVariable String slug) {
        CourseDetailDto detail = courseService.getCourseBySlug(slug);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{slug}/syllabus")
    public ResponseEntity<CurriculumSyllabusDto> getCourseSyllabus(@PathVariable String slug) {
        CurriculumSyllabusDto syllabus = courseService.getCourseSyllabus(slug);
        return ResponseEntity.ok(syllabus);
    }

    @GetMapping("/{slug}/modules")
    public ResponseEntity<List<CurriculumModuleDto>> getCourseModules(@PathVariable String slug) {
        List<CurriculumModuleDto> modules = courseService.getCourseModules(slug);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{slug}/lessons/{lessonId}/preview")
    public ResponseEntity<LessonPreviewDto> getLessonPreview(
            @PathVariable String slug,
            @PathVariable UUID lessonId
    ) {
        LessonPreviewDto preview = courseService.getLessonPreview(slug, lessonId);
        return ResponseEntity.ok(preview);
    }

    @PostMapping("/{courseId}/versions/{versionNumber}/publish")
    @PreAuthorize("hasAuthority('courses:publish') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> publishCurriculumVersion(
            @PathVariable UUID courseId,
            @PathVariable int versionNumber
    ) {
        courseService.publishCurriculumVersion(courseId, versionNumber);
        return ResponseEntity.noContent().build();
    }
}
