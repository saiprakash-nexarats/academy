package com.nexarats.academy.academic;

import com.nexarats.academy.academic.dto.*;
import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.CurriculumModuleRepository;
import com.nexarats.academy.academic.repository.CurriculumVersionRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.academic.service.CourseCatalogCacheService;
import com.nexarats.academy.academic.service.CourseService;
import com.nexarats.academy.common.tenant.TenantContext;
import com.nexarats.academy.tenant.Tenant;
import com.nexarats.academy.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class CourseCatalogIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CurriculumVersionRepository versionRepository;

    @Autowired
    private CurriculumModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseCatalogCacheService cacheService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cacheService.evictAllCourseCache();
        TenantContext.clear();
    }

    @Test
    @DisplayName("DB & Flyway V8: Seeded course catalog, curriculum versions, modules and lessons exist")
    void testFlywayV8SchemaAndCourseSeedCounts() {
        List<Course> courses = courseRepository.findByStatusOrderByDisplayOrderAsc("PUBLISHED");
        assertThat(courses).hasSizeGreaterThanOrEqualTo(8);

        List<String> expectedCourseSlugs = List.of(
                "react-nextjs-architecture",
                "genai-llm-agents",
                "figma-enterprise-design-systems",
                "cloud-devops-kubernetes",
                "backend-microservices-kafka",
                "data-analytics-bi-sql",
                "growth-marketing-attribution",
                "cyber-security-secure-coding"
        );

        for (String slug : expectedCourseSlugs) {
            Optional<Course> c = courseRepository.findBySlug(slug);
            assertThat(c).as("Seeded course exists: " + slug).isPresent();
            assertThat(c.get().getStatus()).isEqualTo("PUBLISHED");
            assertThat(c.get().getPublishedVersionId()).isNotNull();

            // Verify published curriculum version
            Optional<CurriculumVersion> ver = versionRepository.findById(c.get().getPublishedVersionId());
            assertThat(ver).isPresent();

            List<CurriculumModule> mods = moduleRepository.findByVersionIdWithLessons(ver.get().getId());
            assertThat(mods).hasSize(4);

            // Verify lessons within each module
            for (CurriculumModule mod : mods) {
                assertThat(mod.getLessons()).hasSize(4);
            }
            boolean hasPreviewableLesson = mods.stream().flatMap(m -> m.getLessons().stream()).anyMatch(Lesson::isPreviewable);
            assertThat(hasPreviewableLesson).as("Course has previewable lessons").isTrue();
        }
    }

    @Test
    @DisplayName("API: Public access to course catalog returns 200 on canonical and alias routes")
    void testPublicCourseListingWithoutAuthentication() throws Exception {
        // Canonical route
        mockMvc.perform(get("/api/v1/academic/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.items[0].slug").isString())
                .andExpect(jsonPath("$.items[0].instructor.name").isString());

        // Alias route
        mockMvc.perform(get("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(8))));
    }

    @Test
    @DisplayName("API & Filters: Category and skill-level filtering return exact subsets")
    void testCourseFilteringByCategoryAndLevel() throws Exception {
        // Filter by Category
        mockMvc.perform(get("/api/v1/academic/courses")
                        .param("category", "AI & Machine Learning")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].slug").value("genai-llm-agents"));

        // Filter by Skill Level
        mockMvc.perform(get("/api/v1/academic/courses")
                        .param("level", "Intermediate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("API & Search: Search by technology tag, title and instructor")
    void testCourseSearchKeyword() throws Exception {
        // Search by keyword "Kafka"
        mockMvc.perform(get("/api/v1/academic/courses")
                        .param("search", "Kafka")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].slug").value("backend-microservices-kafka"));

        // Search by instructor "Arjun Mehta"
        mockMvc.perform(get("/api/v1/academic/courses")
                        .param("search", "Arjun Mehta")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].slug").value("react-nextjs-architecture"));
    }

    @Test
    @DisplayName("API: Detail lookup by slug returns complete metadata and related programs")
    void testCourseDetailBySlug() throws Exception {
        mockMvc.perform(get("/api/v1/academic/courses/react-nextjs-architecture")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("react-nextjs-architecture"))
                .andExpect(jsonPath("$.title").value("Advanced React 19, Next.js 15 & Modern Web Architecture"))
                .andExpect(jsonPath("$.instructor.name").value("Arjun Mehta"))
                .andExpect(jsonPath("$.instructor.company").value("Google"))
                .andExpect(jsonPath("$.learningOutcomes", hasSize(4)))
                .andExpect(jsonPath("$.techStack", hasSize(6)))
                .andExpect(jsonPath("$.relatedPrograms", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("API: Nonexistent course slug returns 404 NOT_FOUND")
    void testMissingCourseReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/academic/courses/unknown-non-existent-course")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Syllabus Engine: Public syllabus returns ordered modules, lessons, and lock flags")
    void testPublicSyllabusView() throws Exception {
        mockMvc.perform(get("/api/v1/academic/courses/react-nextjs-architecture/syllabus")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseSlug").value("react-nextjs-architecture"))
                .andExpect(jsonPath("$.totalModules").value(4))
                .andExpect(jsonPath("$.totalLessons").value(16))
                .andExpect(jsonPath("$.modules[0].number").value("01"))
                .andExpect(jsonPath("$.modules[0].topics", hasSize(4)))
                .andExpect(jsonPath("$.modules[0].lessons", hasSize(4)))
                .andExpect(jsonPath("$.modules[0].lessons[0].isPreviewable").value(true))
                .andExpect(jsonPath("$.modules[0].lessons[0].isLocked").value(false))
                .andExpect(jsonPath("$.modules[0].lessons[1].isPreviewable").value(false))
                .andExpect(jsonPath("$.modules[0].lessons[1].isLocked").value(true));
    }

    @Test
    @DisplayName("Lesson Security: Previewable lesson returns preview content publicly")
    void testPreviewableLessonContentAccess() throws Exception {
        Course course = courseRepository.findBySlug("react-nextjs-architecture").orElseThrow();
        List<CurriculumModule> mods = moduleRepository.findByVersionIdWithLessons(course.getPublishedVersionId());
        Lesson previewableLesson = mods.getFirst().getLessons().getFirst();
        assertThat(previewableLesson.isPreviewable()).isTrue();

        mockMvc.perform(get("/api/v1/academic/courses/react-nextjs-architecture/lessons/" + previewableLesson.getId() + "/preview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessonId").value(previewableLesson.getId().toString()))
                .andExpect(jsonPath("$.title").value("Virtual DOM Reconciliation"))
                .andExpect(jsonPath("$.previewContent").isNotEmpty());
    }

    @Test
    @DisplayName("Lesson Security: Locked lesson returns 403 Forbidden with enrollment required message")
    void testLockedLessonContentReturnsForbidden() throws Exception {
        Course course = courseRepository.findBySlug("react-nextjs-architecture").orElseThrow();
        List<CurriculumModule> mods = moduleRepository.findByVersionIdWithLessons(course.getPublishedVersionId());
        Lesson lockedLesson = mods.getFirst().getLessons().get(1); // Lesson 2 is locked
        assertThat(lockedLesson.isPreviewable()).isFalse();

        mockMvc.perform(get("/api/v1/academic/courses/react-nextjs-architecture/lessons/" + lockedLesson.getId() + "/preview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Unauthenticated publication request returns 401 Unauthorized")
    void testCurriculumPublishingRequiresAuthority() throws Exception {
        UUID fakeCourseId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/academic/courses/" + fakeCourseId + "/versions/2/publish")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Version Control: Atomic publication switches live curriculum and evicts cache")
    @WithMockUser(authorities = {"courses:publish"})
    void testAtomicCurriculumPublication() {
        Course course = courseRepository.findBySlug("figma-enterprise-design-systems").orElseThrow();
        UUID originalVersionId = course.getPublishedVersionId();

        // 0. Clean up stale version 2 from previous runs if any
        versionRepository.findByCourseIdAndVersionNumber(course.getId(), 2)
                .ifPresent(v -> {
                    course.setPublishedVersionId(originalVersionId);
                    courseRepository.save(course);
                    versionRepository.delete(v);
                });

        // 1. Prepare new draft version 2
        CurriculumVersion v2 = new CurriculumVersion();
        v2.setCourse(course);
        v2.setVersionNumber(2);
        v2.setVersionLabel("v2.0.0");
        v2.setStatus("DRAFT");
        v2.setChangelog("2026 Enterprise Figma Design Tokens Overhaul");
        CurriculumVersion savedV2 = versionRepository.save(v2);

        CurriculumModule v2Mod = new CurriculumModule();
        v2Mod.setVersion(savedV2);
        v2Mod.setModuleNumber("01");
        v2Mod.setTitle("Next-Gen Variables and Tokens Studio");
        v2Mod.setDurationLabel("2 Weeks");
        v2Mod.setDisplayOrder(1);
        CurriculumModule savedMod = moduleRepository.save(v2Mod);

        Lesson v2Lesson = new Lesson();
        v2Lesson.setModule(savedMod);
        v2Lesson.setLessonTitle("Design Token CI/CD Automation");
        v2Lesson.setSummary("Synchronizing Figma variables to GitHub repositories.");
        v2Lesson.setDurationMinutes(45);
        v2Lesson.setPreviewable(true);
        v2Lesson.setDisplayOrder(1);
        lessonRepository.save(v2Lesson);

        try {
            // Verify public syllabus still shows original version 1
            CurriculumSyllabusDto initialSyllabus = courseService.getCourseSyllabus(course.getSlug());
            assertThat(initialSyllabus.versionLabel()).isEqualTo("v1.0.0");

            // 2. Publish version 2 atomically
            courseService.publishCurriculumVersion(course.getId(), 2);

            // 3. Verify public syllabus now serves version 2
            CurriculumSyllabusDto updatedSyllabus = courseService.getCourseSyllabus(course.getSlug());
            assertThat(updatedSyllabus.versionLabel()).isEqualTo("v2.0.0");
            assertThat(updatedSyllabus.modules().getFirst().title()).isEqualTo("Next-Gen Variables and Tokens Studio");

        } finally {
            // Restore original version
            course.setPublishedVersionId(originalVersionId);
            courseRepository.save(course);
            try {
                lessonRepository.delete(v2Lesson);
                moduleRepository.delete(savedMod);
                versionRepository.delete(savedV2);
            } catch (Exception ignored) {
            }
            cacheService.evictCourseBySlug(course.getSlug());
        }
    }

    @Test
    @DisplayName("Redis: Course list, detail, and syllabus caching work with eviction")
    void testRedisCachingMissHitAndInvalidation() {
        String testSlug = "cloud-devops-kubernetes";

        // 1. Syllabus cache miss -> hit
        assertThat(cacheService.getCachedSyllabus(testSlug)).isEmpty();
        CurriculumSyllabusDto syllabus = courseService.getCourseSyllabus(testSlug);
        assertThat(syllabus).isNotNull();

        Optional<CurriculumSyllabusDto> cached = cacheService.getCachedSyllabus(testSlug);
        assertThat(cached).isPresent();
        assertThat(cached.get().courseSlug()).isEqualTo(testSlug);

        // 2. Invalidate
        cacheService.evictCourseBySlug(testSlug);
        assertThat(cacheService.getCachedSyllabus(testSlug)).isEmpty();
    }

    @Test
    @DisplayName("Tenant Isolation: Tenant-owned private courses are excluded from global catalog")
    void testTenantIsolationOnCourses() {
        Tenant tA = tenantRepository.save(new Tenant("corp-c-" + UUID.randomUUID().toString().substring(0, 6), "Alpha Corp"));
        Tenant tB = tenantRepository.save(new Tenant("corp-d-" + UUID.randomUUID().toString().substring(0, 6), "Beta Corp"));
        UUID tenantA = tA.getId();
        UUID tenantB = tB.getId();

        String privateCourseSlug = "private-corp-course-" + UUID.randomUUID().toString().substring(0, 6);
        Course privateCourse = new Course();
        privateCourse.setSlug(privateCourseSlug);
        privateCourse.setTitle("Private Enterprise Security Architecture");
        privateCourse.setCategory("Cyber Security");
        privateCourse.setLevel("Advanced");
        privateCourse.setDurationWeeks(4);
        privateCourse.setShortDescription("Private course for Tenant A");
        privateCourse.setImageAssetKey("courseFullstackImg");
        privateCourse.setInstructorName("Staff Engineer");
        privateCourse.setInstructorCompany("Private Corp");
        privateCourse.setStatus("PUBLISHED");
        privateCourse.setTenantId(tenantA);
        courseRepository.save(privateCourse);

        try {
            // 1. Global unauthenticated access: private course is excluded
            TenantContext.clear();
            PaginatedResponseDto<CourseSummaryDto> globalList = courseService.getPublishedCourses(
                    null, null, null, null, 0, 50, "default"
            );
            boolean globalContains = globalList.items().stream().anyMatch(c -> c.slug().equals(privateCourseSlug));
            assertThat(globalContains).isFalse();

            // Direct access returns 404
            assertThatThrownBy(() -> courseService.getCourseBySlug(privateCourseSlug))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404 NOT_FOUND");

            // 2. Tenant B context: private course is excluded
            TenantContext.setTenantId(tenantB);
            assertThatThrownBy(() -> courseService.getCourseBySlug(privateCourseSlug))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404 NOT_FOUND");

            // 3. Tenant A context: private course is accessible
            TenantContext.setTenantId(tenantA);
            CourseDetailDto detail = courseService.getCourseBySlug(privateCourseSlug);
            assertThat(detail.title()).isEqualTo("Private Enterprise Security Architecture");

        } finally {
            TenantContext.clear();
            courseRepository.delete(privateCourse);
            tenantRepository.delete(tA);
            tenantRepository.delete(tB);
        }
    }
}
