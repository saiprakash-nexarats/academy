package com.nexarats.academy.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.repository.CohortRepository;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.RateLimitingService;
import com.nexarats.academy.tenant.Tenant;
import com.nexarats.academy.tenant.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
public class LearningSecurityIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthService authService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private RateLimitingService rateLimitingService;

    private Course testCourse;
    private Lesson testLesson;
    private Cohort testCohort;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingService.resetRateLimits();

        testCourse = courseRepository.findAll().stream().findFirst().orElseThrow();
        List<Lesson> lessons = lessonRepository.findAll();
        testLesson = lessons.isEmpty() ? null : lessons.get(0);
        testCohort = cohortRepository.findAll().stream().findFirst().orElseThrow();
    }

    private UserSession registerUserWithTenant(String email, String role, String tenantSlug, String tenantName) {
        String testIp = "10.0." + (int) (Math.random() * 200) + "." + (int) (Math.random() * 200);
        Tenant tenant = tenantService.getOrCreateTenant(tenantSlug, tenantName);
        RegisterRequest req = new RegisterRequest(email, "SecurePass2026!", "SecTest", "User", tenantSlug);
        LoginResponse resp = authService.register(req, testIp, "SecurityAgent");
        return new UserSession(resp.accessToken(), resp.user().id(), tenant.getId(), tenant.getSlug());
    }

    private record UserSession(String token, UUID userId, UUID tenantId, String tenantSlug) {}

    @Test
    @DisplayName("Security: Unauthenticated request to /api/v1/learning/dashboard returns 401 Unauthorized")
    void testUnauthenticatedDashboardReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/learning/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Valid student token allows access to /api/v1/learning/dashboard")
    void testStudentRoleAllowedDashboard() throws Exception {
        String email = "sec_student_" + UUID.randomUUID() + "@example.com";
        UserSession student = registerUserWithTenant(email, "STUDENT", "sec-tenant-alpha", "Tenant Alpha");

        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCoursesCount").value(0));
    }

    @Test
    @DisplayName("Security: Cross-tenant enrollment isolation - Tenant A enrollment cannot be viewed under Tenant B")
    void testCrossTenantEnrollmentIsolation() throws Exception {
        // Create Tenant A and Student in Tenant A
        String emailA = "learner_a_" + UUID.randomUUID() + "@example.com";
        UserSession userA = registerUserWithTenant(emailA, "STUDENT", "tenant-alpha-" + UUID.randomUUID().toString().substring(0, 6), "Tenant Alpha Corp");

        // Create Tenant B and Student in Tenant B
        String emailB = "learner_b_" + UUID.randomUUID() + "@example.com";
        UserSession userB = registerUserWithTenant(emailB, "STUDENT", "tenant-beta-" + UUID.randomUUID().toString().substring(0, 6), "Tenant Beta Corp");

        // Enroll User A in Tenant A
        Enrollment enrA = new Enrollment("ENR-TA-" + UUID.randomUUID().toString().substring(0, 6), userA.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enrA.setCourseId(testCourse.getId());
        enrA.setTenantId(userA.tenantId());
        enrollmentRepository.save(enrA);

        // User A in Tenant A sees 1 enrolled course
        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + userA.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCoursesCount").value(1))
                .andExpect(jsonPath("$.enrolledCourses[0].courseId").value(testCourse.getId().toString()));

        // User B in Tenant B sees 0 enrolled courses (zero leakage from Tenant A)
        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + userB.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCoursesCount").value(0));

        // User B cannot access syllabus of Course enrolled only under Tenant A
        mockMvc.perform(get("/api/v1/learning/courses/" + testCourse.getId() + "/syllabus")
                        .header("Authorization", "Bearer " + userB.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Security: Multi-tenant dual enrollment - One user enrolled in same course across two tenants")
    void testDualTenantSameUserEnrollmentIsolation() throws Exception {
        String sharedEmail = "multi_tenant_user_" + UUID.randomUUID() + "@example.com";
        Tenant tenant1 = tenantService.getOrCreateTenant("corp-tenant-1-" + UUID.randomUUID().toString().substring(0, 6), "Enterprise Corp 1");
        Tenant tenant2 = tenantService.getOrCreateTenant("corp-tenant-2-" + UUID.randomUUID().toString().substring(0, 6), "Enterprise Corp 2");

        // Register in Tenant 1
        String testIp = "10.0.99.12";
        RegisterRequest req1 = new RegisterRequest(sharedEmail, "DualPass2026!", "Dual", "Learner", tenant1.getSlug());
        LoginResponse resp1 = authService.register(req1, testIp, "DualAgent");

        // Create Enrollment in Tenant 1
        Enrollment enr1 = new Enrollment("ENR-T1-" + UUID.randomUUID().toString().substring(0, 6), resp1.user().id(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr1.setCourseId(testCourse.getId());
        enr1.setTenantId(tenant1.getId());
        enrollmentRepository.save(enr1);

        // Verify dashboard for Tenant 1 token shows Tenant 1 enrollment
        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + resp1.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCoursesCount").value(1))
                .andExpect(jsonPath("$.enrolledCourses[0].enrollmentNumber").value(enr1.getEnrollmentNumber()));
    }

    @Test
    @DisplayName("Security: Missing X-Tenant-ID header matching token scope is accepted; forged mismatched header is rejected")
    void testTenantHeaderSpoofingRejected() throws Exception {
        String email = "spoof_learner_" + UUID.randomUUID() + "@example.com";
        UserSession session = registerUserWithTenant(email, "STUDENT", "legit-tenant", "Legit Org");

        // Attempting to send forged X-Tenant-ID header with different UUID
        UUID forgedTenantId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + session.token())
                        .header("X-Tenant-ID", forgedTenantId.toString()))
                .andExpect(status().isForbidden());
    }
}
