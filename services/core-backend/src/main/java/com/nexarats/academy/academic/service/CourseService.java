package com.nexarats.academy.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.academic.dto.*;
import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.CurriculumModuleRepository;
import com.nexarats.academy.academic.repository.CurriculumVersionRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.common.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final CurriculumVersionRepository versionRepository;
    private final CurriculumModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CourseCatalogCacheService cacheService;
    private final ObjectMapper objectMapper;

    public CourseService(
            CourseRepository courseRepository,
            CurriculumVersionRepository versionRepository,
            CurriculumModuleRepository moduleRepository,
            LessonRepository lessonRepository,
            CourseCatalogCacheService cacheService,
            ObjectMapper objectMapper
    ) {
        this.courseRepository = courseRepository;
        this.versionRepository = versionRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }

    public PaginatedResponseDto<CourseSummaryDto> getPublishedCourses(
            String category,
            String level,
            Boolean featured,
            String search,
            int page,
            int size,
            String sortBy
    ) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > 100) {
            size = 20;
        }

        String sanitizedCategory = (category != null && !category.isBlank()) ? category.trim() : null;
        String sanitizedLevel = (level != null && !level.isBlank()) ? level.trim() : null;
        String sanitizedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        if (sanitizedSearch != null && sanitizedSearch.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query exceeds maximum length of 100 characters");
        }

        UUID tenantId = TenantContext.getTenantId();
        String tenantKey = tenantId != null ? tenantId.toString() : "global";

        Sort sort = Sort.by(Sort.Direction.ASC, "displayOrder");
        if ("rating".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.ASC, "displayOrder"));
        } else if ("title".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "title");
        }

        String cacheKey = String.format("t=%s:cat=%s:lvl=%s:feat=%s:q=%s:p=%d:s=%d:sort=%s",
                tenantKey,
                sanitizedCategory != null ? sanitizedCategory : "all",
                sanitizedLevel != null ? sanitizedLevel : "all",
                featured != null ? featured : "all",
                sanitizedSearch != null ? sanitizedSearch.toLowerCase() : "",
                page,
                size,
                sortBy != null ? sortBy.toLowerCase() : "default"
        );

        Optional<PaginatedResponseDto<CourseSummaryDto>> cached = cacheService.getCachedCourses(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> coursePage = courseRepository.findPublishedWithFiltersPaged(
                tenantId,
                sanitizedCategory,
                sanitizedLevel,
                featured,
                sanitizedSearch,
                pageable
        );

        List<CourseSummaryDto> items = coursePage.getContent().stream()
                .map(this::mapToSummaryDto)
                .toList();

        PaginatedResponseDto<CourseSummaryDto> response = PaginatedResponseDto.of(
                items,
                coursePage.getTotalElements(),
                page,
                size
        );

        cacheService.cacheCourses(cacheKey, response);
        return response;
    }

    public CourseDetailDto getCourseBySlug(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course identifier cannot be empty");
        }

        String sanitized = identifier.trim().toLowerCase();
        Optional<CourseDetailDto> cached = cacheService.getCachedCourseDetail(sanitized);
        if (cached.isPresent()) {
            return cached.get();
        }

        Course course = findCourseByIdentifier(sanitized);
        validateTenantAccess(course);

        CourseDetailDto detailDto = mapToDetailDto(course);
        cacheService.cacheCourseDetail(course.getSlug(), detailDto);
        return detailDto;
    }

    public CurriculumSyllabusDto getCourseSyllabus(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course identifier cannot be empty");
        }

        String sanitized = identifier.trim().toLowerCase();
        Optional<CurriculumSyllabusDto> cached = cacheService.getCachedSyllabus(sanitized);
        if (cached.isPresent()) {
            return cached.get();
        }

        Course course = findCourseByIdentifier(sanitized);
        validateTenantAccess(course);

        UUID versionId = course.getPublishedVersionId();
        if (versionId == null) {
            // Fallback: look up published version by course id
            CurriculumVersion ver = versionRepository.findByCourseIdAndStatus(course.getId(), "PUBLISHED")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No published curriculum version available for course: " + identifier));
            versionId = ver.getId();
        }

        CurriculumVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curriculum version not found for course: " + identifier));

        List<CurriculumModule> modules = moduleRepository.findByVersionIdWithLessons(versionId);

        List<CurriculumModuleDto> moduleDtos = modules.stream()
                .map(this::mapToModuleDto)
                .toList();

        int totalLessons = moduleDtos.stream().mapToInt(CurriculumModuleDto::lessonsCount).sum();

        CurriculumSyllabusDto syllabusDto = new CurriculumSyllabusDto(
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                version.getVersionLabel(),
                version.getPublishedAt(),
                moduleDtos.size(),
                totalLessons,
                moduleDtos
        );

        cacheService.cacheSyllabus(course.getSlug(), syllabusDto);
        return syllabusDto;
    }

    public List<CurriculumModuleDto> getCourseModules(String identifier) {
        return getCourseSyllabus(identifier).modules();
    }

    public LessonPreviewDto getLessonPreview(String identifier, UUID lessonId) {
        if (identifier == null || identifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course identifier cannot be empty");
        }
        if (lessonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson ID cannot be null");
        }

        String sanitized = identifier.trim().toLowerCase();
        Optional<LessonPreviewDto> cached = cacheService.getCachedLessonPreview(sanitized, lessonId);
        if (cached.isPresent()) {
            return cached.get();
        }

        Course course = findCourseByIdentifier(sanitized);
        validateTenantAccess(course);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found with ID: " + lessonId));

        // Enforce that lesson belongs to this course's published curriculum
        CurriculumModule module = lesson.getModule();
        if (module == null || module.getVersion() == null || !module.getVersion().getCourse().getId().equals(course.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson does not belong to the requested course");
        }

        // Enforce lesson preview boundary: locked lessons return 403 Forbidden!
        if (!lesson.isPreviewable()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This lesson is locked. Enrollment is required to access private curriculum content."
            );
        }

        LessonPreviewDto previewDto = new LessonPreviewDto(
                lesson.getId(),
                course.getSlug(),
                module.getModuleNumber(),
                lesson.getLessonTitle(),
                lesson.getSummary(),
                lesson.getLessonType(),
                lesson.getDurationMinutes(),
                lesson.getPreviewContent() != null ? lesson.getPreviewContent() : lesson.getSummary()
        );

        cacheService.cacheLessonPreview(course.getSlug(), lesson.getId(), previewDto);
        return previewDto;
    }

    @Transactional
    public void publishCurriculumVersion(UUID courseId, int versionNumber) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        CurriculumVersion version = versionRepository.findByCourseIdAndVersionNumber(courseId, versionNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version " + versionNumber + " not found for course"));

        // Atomically publish
        version.setStatus("PUBLISHED");
        version.setPublishedAt(Instant.now());
        versionRepository.save(version);

        course.setPublishedVersionId(version.getId());
        course.setUpdatedAt(Instant.now());
        courseRepository.save(course);

        cacheService.evictCourseBySlug(course.getSlug());
        log.info("Curriculum version {} published atomically for course {}", versionNumber, course.getSlug());
    }

    private Course findCourseByIdentifier(String identifier) {
        Optional<Course> courseOpt = courseRepository.findBySlugAndStatus(identifier, "PUBLISHED");
        if (courseOpt.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(identifier.trim());
                courseOpt = courseRepository.findById(uuid)
                        .filter(c -> "PUBLISHED".equals(c.getStatus()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return courseOpt.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Published course not found with identifier: " + identifier)
        );
    }

    private void validateTenantAccess(Course course) {
        UUID currentTenant = TenantContext.getTenantId();
        if (course.getTenantId() != null && (currentTenant == null || !currentTenant.equals(course.getTenantId()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found");
        }
    }

    private CourseSummaryDto mapToSummaryDto(Course course) {
        return new CourseSummaryDto(
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                course.getCategory(),
                course.getLevel(),
                course.getDurationWeeks(),
                course.getLessonsCount(),
                course.getProjectsCount(),
                course.getRating(),
                course.getReviewsCount(),
                course.getEnrolledStudents(),
                new CourseInstructorDto(course.getInstructorName(), course.getInstructorCompany()),
                course.getImageAssetKey(),
                course.getBadge(),
                course.getShortDescription(),
                parseJsonList(course.getTechStack()),
                course.isFeatured(),
                course.getDisplayOrder()
        );
    }

    private CourseDetailDto mapToDetailDto(Course course) {
        String versionLabel = "v1.0.0";
        int modulesCount = 0;
        if (course.getPublishedVersionId() != null) {
            Optional<CurriculumVersion> vOpt = versionRepository.findById(course.getPublishedVersionId());
            if (vOpt.isPresent()) {
                versionLabel = vOpt.get().getVersionLabel();
                modulesCount = vOpt.get().getModules().size();
            }
        }

        List<CourseProgramRefDto> relatedPrograms = course.getPrograms().stream()
                .map(p -> new CourseProgramRefDto(p.getId(), p.getSlug(), p.getTitle()))
                .toList();

        return new CourseDetailDto(
                course.getId(),
                course.getSlug(),
                course.getTitle(),
                course.getCategory(),
                course.getLevel(),
                course.getDurationWeeks(),
                course.getLessonsCount(),
                course.getProjectsCount(),
                course.getRating(),
                course.getReviewsCount(),
                course.getEnrolledStudents(),
                new CourseInstructorDto(course.getInstructorName(), course.getInstructorCompany()),
                course.getImageAssetKey(),
                course.getBadge(),
                course.getShortDescription(),
                course.getDetailedDescription(),
                parseJsonList(course.getTechStack()),
                parseJsonList(course.getLearningOutcomes()),
                course.getPrerequisites(),
                versionLabel,
                modulesCount,
                relatedPrograms,
                course.isFeatured(),
                course.getDisplayOrder(),
                course.getPublishedAt()
        );
    }

    private CurriculumModuleDto mapToModuleDto(CurriculumModule module) {
        List<LessonSummaryDto> lessons = module.getLessons().stream()
                .map(this::mapToLessonSummaryDto)
                .toList();

        List<String> topics = lessons.stream()
                .map(LessonSummaryDto::title)
                .toList();

        return new CurriculumModuleDto(
                module.getId(),
                module.getModuleNumber(),
                module.getTitle(),
                module.getDurationLabel(),
                module.getDisplayOrder(),
                lessons.size(),
                topics,
                lessons
        );
    }

    private LessonSummaryDto mapToLessonSummaryDto(Lesson lesson) {
        return new LessonSummaryDto(
                lesson.getId(),
                lesson.getLessonTitle(),
                lesson.getSummary(),
                lesson.getLessonType(),
                lesson.getDurationMinutes(),
                lesson.isPreviewable(),
                lesson.getDisplayOrder(),
                !lesson.isPreviewable()
        );
    }

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            log.warn("Failed to parse JSON string list: {}", json);
            return Collections.emptyList();
        }
    }
}
