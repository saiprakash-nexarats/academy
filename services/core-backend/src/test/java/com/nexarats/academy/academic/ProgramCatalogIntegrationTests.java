package com.nexarats.academy.academic;

import com.nexarats.academy.academic.dto.PaginatedResponseDto;
import com.nexarats.academy.academic.dto.ProgramCategoryDto;
import com.nexarats.academy.academic.dto.ProgramDetailDto;
import com.nexarats.academy.academic.dto.ProgramSummaryDto;
import com.nexarats.academy.academic.model.Program;
import com.nexarats.academy.academic.model.ProgramCategory;
import com.nexarats.academy.academic.repository.ProgramCategoryRepository;
import com.nexarats.academy.academic.repository.ProgramRepository;
import com.nexarats.academy.academic.service.ProgramCatalogCacheService;
import com.nexarats.academy.academic.service.ProgramCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class ProgramCatalogIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramCategoryRepository categoryRepository;

    @Autowired
    private ProgramCatalogService catalogService;

    @Autowired
    private ProgramCatalogCacheService cacheService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cacheService.evictAllCatalogCache();
    }

    @Test
    @DisplayName("DB & Flyway: Seeded categories and programs are present with correct row counts")
    void testFlywayAndDatabaseSeedCounts() {
        List<ProgramCategory> categories = categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        assertThat(categories).hasSizeGreaterThanOrEqualTo(4);

        List<Program> publishedPrograms = programRepository.findByStatusOrderByDisplayOrderAsc("PUBLISHED");
        assertThat(publishedPrograms).hasSizeGreaterThanOrEqualTo(8);

        List<String> expectedSlugs = List.of(
                "full-stack", "data-science", "ui-ux", "digital-marketing",
                "cloud-devops", "mobile-app", "gen-ai-engineers", "product-management"
        );

        for (String slug : expectedSlugs) {
            Optional<Program> p = programRepository.findBySlug(slug);
            assertThat(p).as("Seeded program exists: " + slug).isPresent();
            assertThat(p.get().getStatus()).isEqualTo("PUBLISHED");
            assertThat(p.get().getImageAssetKey()).isNotBlank();
        }
    }

    @Test
    @DisplayName("API: Public access without token returns 200 on canonical and alias routes")
    void testPublicCatalogListingWithoutAuthentication() throws Exception {
        // Canonical route
        mockMvc.perform(get("/api/v1/academic/programs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.items[0].slug").isNotEmpty());

        // Alias compatibility route
        mockMvc.perform(get("/api/v1/programs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(8)));
    }

    @Test
    @DisplayName("API: Program detail by stable slug returns 200 with full metadata")
    void testProgramDetailBySlug() throws Exception {
        mockMvc.perform(get("/api/v1/academic/programs/full-stack")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("full-stack"))
                .andExpect(jsonPath("$.title").value("Full Stack Development"))
                .andExpect(jsonPath("$.imageAssetKey").value("course-fullstack"))
                .andExpect(jsonPath("$.categorySlug").value("software-engineering"))
                .andExpect(jsonPath("$.badge").value("Bestseller"))
                .andExpect(jsonPath("$.durationLabel").value("6 Months"))
                .andExpect(jsonPath("$.rating").value(4.90))
                .andExpect(jsonPath("$.isFeatured").value(true));

        // Alias route
        mockMvc.perform(get("/api/v1/programs/full-stack")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("full-stack"));
    }

    @Test
    @DisplayName("API: Unknown program slug returns RFC 7807 404 ProblemDetail")
    void testProgramDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/academic/programs/unknown-program-does-not-exist")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("API: Filter by category returns expected matching subset")
    void testFilteringByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/academic/programs")
                        .param("category", "software-engineering")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.items[*].categorySlug", everyItem(equalTo("software-engineering"))));
    }

    @Test
    @DisplayName("API: Filter by duration returns expected programs")
    void testFilteringByDuration() throws Exception {
        mockMvc.perform(get("/api/v1/academic/programs")
                        .param("duration", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.items[*].durationValue", everyItem(equalTo(4))));
    }

    @Test
    @DisplayName("API: Filter by featured=true returns only popular homepage programs")
    void testFilteringByFeatured() throws Exception {
        mockMvc.perform(get("/api/v1/academic/programs")
                        .param("featured", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(4)))
                .andExpect(jsonPath("$.items[*].isFeatured", everyItem(equalTo(true))));
    }

    @Test
    @DisplayName("API: Search query matches titles or descriptions")
    void testSearchByKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/academic/programs")
                        .param("search", "Python")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.items[0].slug").value("data-science"));
    }

    @Test
    @DisplayName("API: Search query exceeding 100 characters returns 400 Bad Request")
    void testInvalidSearchQueryLengthRejection() throws Exception {
        String longQuery = "a".repeat(101);
        mockMvc.perform(get("/api/v1/academic/programs")
                        .param("search", longQuery)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("API: List active program categories returns 200")
    void testListCategoriesEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/academic/program-categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].slug").isNotEmpty())
                .andExpect(jsonPath("$[0].name").isNotEmpty());

        // Alias route
        mockMvc.perform(get("/api/v1/program-categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))));
    }

    @Test
    @DisplayName("Redis: Cache hit, miss, and isolation work cleanly")
    void testRedisCachingMissHitAndInvalidation() {
        cacheService.evictAllCatalogCache();

        // 1. Initial lookup -> Cache Miss -> Cached in Redis
        ProgramDetailDto detail = catalogService.getProgramBySlug("full-stack");
        assertThat(detail).isNotNull();
        assertThat(detail.slug()).isEqualTo("full-stack");

        // 2. Direct cache lookup -> Cache HIT
        Optional<ProgramDetailDto> cached = cacheService.getCachedProgramDetail("full-stack");
        assertThat(cached).isPresent();
        assertThat(cached.get().title()).isEqualTo("Full Stack Development");

        // 3. Evict specific program -> Cache cleared
        cacheService.evictProgramDetail("full-stack");
        Optional<ProgramDetailDto> evicted = cacheService.getCachedProgramDetail("full-stack");
        assertThat(evicted).isEmpty();

        // 4. Invalidate all catalog cache
        catalogService.getCategories();
        assertThat(cacheService.getCachedCategories()).isPresent();
        cacheService.evictAllCatalogCache();
        assertThat(cacheService.getCachedCategories()).isEmpty();
    }

    @Test
    @DisplayName("Security & Domain: Unpublished draft program is not exposed publicly")
    void testDraftProgramNotExposedPublicly() {
        // Find existing category
        ProgramCategory category = categoryRepository.findBySlug("software-engineering").orElseThrow();

        // Create an unapproved/draft program
        String draftSlug = "draft-internal-" + UUID.randomUUID().toString().substring(0, 6);
        Program draftProgram = new Program();
        draftProgram.setSlug(draftSlug);
        draftProgram.setTitle("Internal Secret Syllabus");
        draftProgram.setShortDescription("Unapproved draft program");
        draftProgram.setImageAssetKey("course-fullstack");
        draftProgram.setDurationValue(3);
        draftProgram.setDurationUnit("MONTHS");
        draftProgram.setDurationLabel("3 Months");
        draftProgram.setLevel("Advanced");
        draftProgram.setRating(BigDecimal.valueOf(4.50));
        draftProgram.setStatus("DRAFT");
        draftProgram.setCategory(category);
        programRepository.save(draftProgram);

        try {
            // Verify public service throws 404 NOT_FOUND for draft program
            assertThatThrownBy(() -> catalogService.getProgramBySlug(draftSlug))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404 NOT_FOUND");

            // Verify draft is omitted from published program listing
            PaginatedResponseDto<ProgramSummaryDto> allPublished = catalogService.getPublishedPrograms(
                    null, null, null, null, 0, 50, "default"
            );
            boolean containsDraft = allPublished.items().stream().anyMatch(p -> p.slug().equals(draftSlug));
            assertThat(containsDraft).isFalse();
        } finally {
            // Clean up
            programRepository.delete(draftProgram);
        }
    }

    @Autowired
    private com.nexarats.academy.tenant.TenantRepository tenantRepository;

    @Test
    @DisplayName("Task 0.A Tenant Isolation: Cross-tenant programs are strictly isolated and platform-wide queries exclude private tenant records")
    void testCrossTenantProgramIsolationAndScoping() {
        ProgramCategory category = categoryRepository.findBySlug("software-engineering").orElseThrow();
        com.nexarats.academy.tenant.Tenant tA = tenantRepository.save(new com.nexarats.academy.tenant.Tenant(
                "corp-a-" + UUID.randomUUID().toString().substring(0, 6), "Alpha Corp"
        ));
        com.nexarats.academy.tenant.Tenant tB = tenantRepository.save(new com.nexarats.academy.tenant.Tenant(
                "corp-b-" + UUID.randomUUID().toString().substring(0, 6), "Beta Corp"
        ));
        UUID tenantA = tA.getId();
        UUID tenantB = tB.getId();

        String tenantSlug = "tenant-a-private-" + UUID.randomUUID().toString().substring(0, 6);
        Program tenantProgram = new Program();
        tenantProgram.setSlug(tenantSlug);
        tenantProgram.setTitle("Tenant A Private Program");
        tenantProgram.setShortDescription("Private curriculum for Tenant A only");
        tenantProgram.setImageAssetKey("course-fullstack");
        tenantProgram.setDurationValue(4);
        tenantProgram.setDurationUnit("MONTHS");
        tenantProgram.setDurationLabel("4 Months");
        tenantProgram.setLevel("Intermediate");
        tenantProgram.setRating(BigDecimal.valueOf(4.90));
        tenantProgram.setStatus("PUBLISHED");
        tenantProgram.setCategory(category);
        tenantProgram.setTenantId(tenantA);
        programRepository.save(tenantProgram);

        try {
            // 1. Unauthenticated / Global user without tenant context
            com.nexarats.academy.common.tenant.TenantContext.clear();
            PaginatedResponseDto<ProgramSummaryDto> globalList = catalogService.getPublishedPrograms(
                    null, null, null, null, 0, 50, "default"
            );
            boolean globalContainsPrivate = globalList.items().stream().anyMatch(p -> p.slug().equals(tenantSlug));
            assertThat(globalContainsPrivate).as("Global catalog must exclude tenant-owned program").isFalse();

            // Direct slug lookup by unauthenticated global user returns 404
            assertThatThrownBy(() -> catalogService.getProgramBySlug(tenantSlug))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404 NOT_FOUND");

            // 2. Unrelated Tenant B context
            com.nexarats.academy.common.tenant.TenantContext.setTenantId(tenantB);
            PaginatedResponseDto<ProgramSummaryDto> tenantBList = catalogService.getPublishedPrograms(
                    null, null, null, null, 0, 50, "default"
            );
            boolean tenantBContainsPrivate = tenantBList.items().stream().anyMatch(p -> p.slug().equals(tenantSlug));
            assertThat(tenantBContainsPrivate).as("Tenant B must not see Tenant A private program").isFalse();

            assertThatThrownBy(() -> catalogService.getProgramBySlug(tenantSlug))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("404 NOT_FOUND");

            // 3. Authorized Tenant A context
            com.nexarats.academy.common.tenant.TenantContext.setTenantId(tenantA);
            PaginatedResponseDto<ProgramSummaryDto> tenantAList = catalogService.getPublishedPrograms(
                    null, null, null, null, 0, 50, "default"
            );
            boolean tenantAContainsPrivate = tenantAList.items().stream().anyMatch(p -> p.slug().equals(tenantSlug));
            assertThat(tenantAContainsPrivate).as("Tenant A can access its own private program").isTrue();

            ProgramDetailDto detail = catalogService.getProgramBySlug(tenantSlug);
            assertThat(detail.title()).isEqualTo("Tenant A Private Program");
        } finally {
            com.nexarats.academy.common.tenant.TenantContext.clear();
            programRepository.delete(tenantProgram);
            tenantRepository.delete(tA);
            tenantRepository.delete(tB);
        }
    }
}
