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
import com.nexarats.academy.common.security.JwtTokenService;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.model.Role;
import com.nexarats.academy.identity.model.TenantMembership;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.RoleRepository;
import com.nexarats.academy.identity.repository.TenantMembershipRepository;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.RateLimitingService;
import com.nexarats.academy.identity.service.SessionService;
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

/**
 * Spring Security & Multi-Tenant Authorization Integration Tests.
 *
 * Verifies:
 * 1. Unauthenticated requests return 401 Unauthorized.
 * 2. Authenticated students with valid tenant token can access dashboard (200 OK).
 * 3. Cross-tenant isolation: Users in Tenant A cannot see courses/enrollments or access syllabus from Tenant B.
 * 4. Multi-tenant dual enrollment: The same user enrolled across different tenants maintains strictly isolated progress.
 * 5. Tenant ID header spoofing: Mismatched X-Tenant-ID headers are rejected (403 Forbidden).
 */
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
    private UserRepository userRepository;

    @Autowired
    private TenantMembershipRepository membershipRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtTokenService jwtTokenService;

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
        RegisterRequest req = new RegisterRequest(email, "SecurePass2026!", "SecTest", "User", null);
        LoginResponse resp = authService.register(req, testIp, "SecurityAgent");
        User user = userRepository.findById(resp.user().id()).orElseThrow();

        // Create membership in target tenant
        Role studentRole = roleRepository.findByCode("ROLE_STUDENT")
                .orElseThrow(() -> new IllegalStateException("System role ROLE_STUDENT is not seeded"));
        TenantMembership membership = new TenantMembership(tenant, user, "ACTIVE");
        membership.addRole(studentRole);
        membershipRepository.save(membership);

        // Generate token scoped specifically to target tenant
        SessionService.RotatedTokenPair tokenPair = sessionService.createSession(user, tenant, testIp, "SecurityAgent", "WEB");
        String token = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                tenant.getId(),
                tenant.getSlug(),
                tokenPair.session().getId(),
                List.of("ROLE_STUDENT"),
                List.of()
        );
        return new UserSession(token, user.getId(), tenant.getId(), tenant.getSlug());
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

        // Register user
        String testIp = "10.0.99.12";
        RegisterRequest req1 = new RegisterRequest(sharedEmail, "DualPass2026!", "Dual", "Learner", null);
        LoginResponse resp1 = authService.register(req1, testIp, "DualAgent");
        User user = userRepository.findById(resp1.user().id()).orElseThrow();

        // Create membership in Tenant 1
        Role studentRole = roleRepository.findByCode("ROLE_STUDENT").orElseThrow();
        TenantMembership m1 = new TenantMembership(tenant1, user, "ACTIVE");
        m1.addRole(studentRole);
        membershipRepository.save(m1);

        // Create membership in Tenant 2
        TenantMembership m2 = new TenantMembership(tenant2, user, "ACTIVE");
        m2.addRole(studentRole);
        membershipRepository.save(m2);

        // Generate token for Tenant 1
        SessionService.RotatedTokenPair pair1 = sessionService.createSession(user, tenant1, testIp, "DualAgent", "WEB");
        String token1 = jwtTokenService.generateAccessToken(
                user.getId(), user.getEmail(), user.getFullName(),
                tenant1.getId(), tenant1.getSlug(), pair1.session().getId(),
                List.of("ROLE_STUDENT"), List.of()
        );

        // Generate token for Tenant 2
        SessionService.RotatedTokenPair pair2 = sessionService.createSession(user, tenant2, testIp, "DualAgent", "WEB");
        String token2 = jwtTokenService.generateAccessToken(
                user.getId(), user.getEmail(), user.getFullName(),
                tenant2.getId(), tenant2.getSlug(), pair2.session().getId(),
                List.of("ROLE_STUDENT"), List.of()
        );

        // Create Enrollment in Tenant 1 only
        Enrollment enr1 = new Enrollment("ENR-T1-" + UUID.randomUUID().toString().substring(0, 6), user.getId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr1.setCourseId(testCourse.getId());
        enr1.setTenantId(tenant1.getId());
        enrollmentRepository.save(enr1);

        // Verify dashboard for Tenant 1 token shows 1 enrolled course
        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCoursesCount").value(1))
                .andExpect(jsonPath("$.enrolledCourses[0].enrollmentNumber").value(enr1.getEnrollmentNumber()));

        // Verify dashboard for Tenant 2 token shows 0 enrolled courses (zero leakage from Tenant 1)
        mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCoursesCount").value(0));
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

    @Test
    @DisplayName("Security: Direct video lesson completion invocation WITHOUT 80% watch time is rejected with 400 Bad Request")
    void testVideoLessonCompletionDirectCallWithoutWatchTimeRejected400() throws Exception {
        String email = "bypass_student_" + UUID.randomUUID() + "@example.com";
        UserSession session = registerUserWithTenant(email, "STUDENT", "bypass-tenant", "Bypass Org");

        // Enroll student in test course
        Enrollment enr = new Enrollment("ENR-BYP-" + UUID.randomUUID().toString().substring(0, 6), session.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(session.tenantId());
        enrollmentRepository.save(enr);

        // Attempt direct completion without watching
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/complete")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(org.hamcrest.Matchers.containsString("Video lesson completion requires at least 80% validated watch time")));
    }

    @Test
    @DisplayName("Security: Direct video lesson completion invocation WITH >= 80% watch time succeeds with 200 OK")
    void testVideoLessonCompletionDirectCallAfterWatchTimeSucceeds200() throws Exception {
        String email = "legit_student_" + UUID.randomUUID() + "@example.com";
        UserSession session = registerUserWithTenant(email, "STUDENT", "legit-tenant-2", "Legit Org 2");

        // Enroll student in test course
        Enrollment enr = new Enrollment("ENR-LEG-" + UUID.randomUUID().toString().substring(0, 6), session.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(session.tenantId());
        enrollmentRepository.save(enr);

        // Create playback session
        String playbackResStr = mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/playback-session")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(playbackResStr);
        String sessionToken = rootNode.get("sessionToken").asText();
        int durationSec = rootNode.has("durationSeconds") ? rootNode.get("durationSeconds").asInt() : 1200;

        // Record progress checkpoints satisfying >= 80%
        int watchTarget = (int) (durationSec * 0.85);
        // Record checkpoint
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"playbackPositionSeconds\":" + watchTarget + ",\"watchTimeDeltaSeconds\":" + watchTarget + ",\"idempotencyKey\":\"legit_chk_1\",\"sessionToken\":null}"))
                .andExpect(status().isOk());

        // Now call markLessonComplete -> succeeds 200 OK
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/complete")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLessonCompleted").value(true));
    }
}
