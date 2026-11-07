package com.nexarats.academy.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.model.Program;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.CurriculumModuleRepository;
import com.nexarats.academy.academic.repository.CurriculumVersionRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.academic.repository.ProgramRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.cohort.model.Cohort;
import com.nexarats.academy.cohort.model.CohortStatus;
import com.nexarats.academy.cohort.repository.CohortRepository;
import com.nexarats.academy.finance.service.TestPaymentGateway;
import com.nexarats.academy.identity.dto.LoginRequest;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.RateLimitingService;
import com.nexarats.academy.learning.dto.PlaybackCheckpointRequest;
import com.nexarats.academy.learning.dto.PlaybackSessionResponse;
import com.nexarats.academy.learning.model.LearnerLessonProgress;
import com.nexarats.academy.learning.model.PlaybackSession;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.repository.PlaybackSessionRepository;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
public class LearningAndVideoDeliveryIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private CurriculumVersionRepository versionRepository;

    @Autowired
    private CurriculumModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PlaybackSessionRepository playbackSessionRepository;

    @Autowired
    private LearnerLessonProgressRepository progressRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private RateLimitingService rateLimitingService;

    private Course testCourse;
    private Lesson testLesson;
    private Lesson testLesson2;
    private Cohort testCohort;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingService.resetRateLimits();

        // Retrieve seeded course, curriculum, and lessons
        testCourse = courseRepository.findBySlug("react-nextjs-architecture")
                .orElseGet(() -> courseRepository.findAll().stream().findFirst().orElseThrow());

        CurriculumVersion ver = versionRepository.findByCourseIdAndStatus(testCourse.getId(), "PUBLISHED")
                .orElseGet(() -> versionRepository.findAll().stream().findFirst().orElseThrow());

        List<CurriculumModule> modules = moduleRepository.findByVersionIdOrderByDisplayOrderAsc(ver.getId());
        CurriculumModule mod = modules.isEmpty() ? moduleRepository.findAll().stream().findFirst().orElseThrow() : modules.get(0);

        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByDisplayOrderAsc(mod.getId());
        if (lessons.isEmpty()) {
            lessons = lessonRepository.findAll();
        }
        testLesson = lessons.get(0);
        testLesson2 = lessons.size() > 1 ? lessons.get(1) : testLesson;

        testCohort = cohortRepository.findAll().stream().findFirst().orElseThrow();
    }

    private UserAuth registerStudent(String email, String firstName, String lastName) {
        String testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);
        RegisterRequest req = new RegisterRequest(email, "StudentPass2026!", firstName, lastName, null);
        LoginResponse resp = authService.register(req, testIp, "TestAgent");
        UUID tenantId = resp.tenant() != null ? resp.tenant().id() : null;

        StudentProfile profile = new StudentProfile(resp.user().id(), tenantId);
        profile.setOnboardingStatus(OnboardingStatus.COMPLETED);
        studentProfileRepository.save(profile);

        return new UserAuth(resp.accessToken(), resp.user().id(), tenantId);
    }

    private record UserAuth(String token, UUID userId, UUID tenantId) {}

    @Test
    @DisplayName("Test 1: Enrolled learner can access dashboard with real stats and continue learning target")
    void testEnrolledLearnerCanAccessDashboard() throws Exception {
        String email = "learner_dash_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Aarav", "Sharma");

        // Create enrollment
        Enrollment enr = new Enrollment("ENR-TEST-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(auth.tenantId());
        enrollmentRepository.save(enr);

        MvcResult result = mockMvc.perform(get("/api/v1/learning/dashboard")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Aarav Sharma"))
                .andExpect(jsonPath("$.enrolledCoursesCount").value(1))
                .andExpect(jsonPath("$.enrolledCourses[0].courseId").value(testCourse.getId().toString()))
                .andExpect(jsonPath("$.enrolledCourses[0].completionPercentage").value(0))
                .andExpect(jsonPath("$.continueLearningTarget").isNotEmpty())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("Advanced React 19");
    }

    @Test
    @DisplayName("Test 2: Non-enrolled user is denied access to private course syllabus and lesson details (403 Forbidden)")
    void testNonEnrolledUserDeniedAccess() throws Exception {
        String email = "guest_user_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Guest", "Student");

        // Attempting to access syllabus without enrollment
        mockMvc.perform(get("/api/v1/learning/courses/" + testCourse.getId() + "/syllabus")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isForbidden());

        // Attempting to access private lesson detail without enrollment
        mockMvc.perform(get("/api/v1/learning/courses/" + testCourse.getId() + "/lessons/" + testLesson.getId())
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 3: Unauthenticated user is denied from private learning endpoints (401 Unauthorized)")
    void testUnauthenticatedUserDeniedLearningEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/learning/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/playback-session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test 4: Authorized playback session creation and verified video streaming")
    void testAuthorizedPlaybackSessionAndStreaming() throws Exception {
        String email = "stream_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Priya", "Nair");

        Enrollment enr = new Enrollment("ENR-TEST-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(auth.tenantId());
        enrollmentRepository.save(enr);

        // 1. Request Playback Session
        MvcResult sessResult = mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/playback-session")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionToken").isNotEmpty())
                .andExpect(jsonPath("$.streamUrl").isNotEmpty())
                .andExpect(jsonPath("$.durationSeconds").isNumber())
                .andReturn();

        String sessionToken = com.jayway.jsonpath.JsonPath.read(sessResult.getResponse().getContentAsString(), "$.sessionToken");
        assertThat(sessionToken).startsWith("vtok_");

        // 2. Stream Full Media
        mockMvc.perform(get("/api/v1/learning/media/stream?token=" + sessionToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "video/mp4"))
                .andExpect(header().string("Accept-Ranges", "bytes"));
    }

    @Test
    @DisplayName("Test 5: Media streaming with HTTP 206 Partial Content range requests")
    void testMediaStreamingByteRangeRequest() throws Exception {
        String email = "range_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Rohan", "Verma");

        Enrollment enr = new Enrollment("ENR-TEST-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(auth.tenantId());
        enrollmentRepository.save(enr);

        MvcResult sessResult = mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/playback-session")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andReturn();

        String sessionToken = com.jayway.jsonpath.JsonPath.read(sessResult.getResponse().getContentAsString(), "$.sessionToken");

        // Request specific byte range: bytes=0-499
        mockMvc.perform(get("/api/v1/learning/media/stream?token=" + sessionToken)
                        .header("Range", "bytes=0-499"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("Content-Range", org.hamcrest.Matchers.startsWith("bytes 0-499/")))
                .andExpect(header().string("Content-Length", "500"));
    }

    @Test
    @DisplayName("Test 6: Media streaming with invalid or expired token is rejected (403 Forbidden)")
    void testMediaStreamingInvalidTokenRejected() throws Exception {
        // Forged or missing token
        mockMvc.perform(get("/api/v1/learning/media/stream?token=invalid_token_12345"))
                .andExpect(status().isForbidden());

        // Expired token
        String email = "exp_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Expired", "Tester");

        Enrollment enr = new Enrollment("ENR-TEST-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(auth.tenantId());
        enrollmentRepository.save(enr);

        PlaybackSession expiredSession = new PlaybackSession(
                "vtok_expired_" + UUID.randomUUID(),
                auth.userId(),
                enr.getId(),
                testLesson.getId(),
                null,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        playbackSessionRepository.save(expiredSession);

        mockMvc.perform(get("/api/v1/learning/media/stream?token=" + expiredSession.getSessionToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 7: Progress checkpoint validation and boundary enforcement")
    void testProgressCheckpointValidation() throws Exception {
        String email = "chk_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Sneha", "Reddy");

        Enrollment enr = new Enrollment("ENR-TEST-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(auth.tenantId());
        enrollmentRepository.save(enr);

        // Valid checkpoint
        PlaybackCheckpointRequest validReq = new PlaybackCheckpointRequest(60, 60, "key_" + UUID.randomUUID());
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastPlaybackPositionSeconds").value(60))
                .andExpect(jsonPath("$.watchTimeSeconds").value(60));

        // Negative playback position rejected
        PlaybackCheckpointRequest negReq = new PlaybackCheckpointRequest(-5, 10, "key_neg");
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negReq)))
                .andExpect(status().isBadRequest());

        // Unrealistic watch delta (> 300s) rejected
        PlaybackCheckpointRequest fakeDeltaReq = new PlaybackCheckpointRequest(100, 600, "key_fake");
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(fakeDeltaReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test 8: Idempotent checkpointing prevents replay attacks and double counting")
    void testIdempotentCheckpointReplayDefense() throws Exception {
        String email = "idemp_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Kabir", "Mehta");

        Enrollment enr = new Enrollment("ENR-TEST-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(auth.tenantId());
        enrollmentRepository.save(enr);

        String idempKey = "idemp_test_" + UUID.randomUUID();
        PlaybackCheckpointRequest req = new PlaybackCheckpointRequest(120, 30, idempKey);

        // First submission
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watchTimeSeconds").value(30));

        // Replay submission with same idempotency key
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/progress")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watchTimeSeconds").value(30)) // Still 30, no double increment!
                .andExpect(jsonPath("$.message").value("Idempotent duplicate ignored."));
    }

    @Test
    @DisplayName("Test 9: Explicit lesson completion and server-authoritative course progress recalculation")
    void testLessonCompletionAndCourseProgressRecalculation() throws Exception {
        String email = "comp_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Neha", "Kapoor");

        Enrollment enr = new Enrollment("ENR-TEST-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(testCourse.getId());
        enr.setTenantId(auth.tenantId());
        enrollmentRepository.save(enr);

        // Record sufficient watch time (>= 80%) for the video lesson
        LearnerLessonProgress prog = new LearnerLessonProgress(auth.userId(), enr.getId(), testCourse.getId(), testLesson.getId());
        prog.setTenantId(auth.tenantId());
        int durSec = testLesson.getDurationMinutes() > 0 ? testLesson.getDurationMinutes() * 60 : 600;
        prog.setWatchTimeSeconds((int) (durSec * 0.9));
        progressRepository.save(prog);

        // Mark lesson 1 complete
        mockMvc.perform(post("/api/v1/learning/lessons/" + testLesson.getId() + "/complete")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLessonCompleted").value(true))
                .andExpect(jsonPath("$.lessonCompletionPercentage").value(100));

        // Check Course Progress
        mockMvc.perform(get("/api/v1/learning/courses/" + testCourse.getId() + "/progress")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedLessonsCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.completionPercentage").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    @DisplayName("Test 10: Program enrollment grants entitlement to associated career track courses")
    void testProgramEnrollmentEntitlement() throws Exception {
        String email = "prog_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Vikram", "Malhotra");

        Program prog = programRepository.findAll().stream().findFirst().orElseThrow();

        // Enroll at Program level (no direct courseId set on enrollment)
        Enrollment progEnr = new Enrollment("ENR-PROG-" + UUID.randomUUID().toString().substring(0, 6), auth.userId(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        progEnr.setProgramId(prog.getId());
        progEnr.setTenantId(auth.tenantId());
        enrollmentRepository.save(progEnr);

        // Query program courses
        List<Course> progCourses = courseRepository.findPublishedCoursesByProgramId(prog.getId());
        if (!progCourses.isEmpty()) {
            Course entitledCourse = progCourses.get(0);

            // Learner should have syllabus access to entitled program course
            mockMvc.perform(get("/api/v1/learning/courses/" + entitledCourse.getId() + "/syllabus")
                            .header("Authorization", "Bearer " + auth.token()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.courseId").value(entitledCourse.getId().toString()));
        }
    }

    @Test
    @DisplayName("Test 11: Phase 5 Safety - TestPaymentGateway strictly rejects production configuration")
    void testPaymentGatewayRejectsProduction() {
        MockEnvironment prodEnv = new MockEnvironment();
        prodEnv.setActiveProfiles("production");

        TestPaymentGateway gateway = new TestPaymentGateway(prodEnv);

        assertThatThrownBy(() -> gateway.createOrder(null, "user@test.com", "Test User"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("strictly disabled in production");

        assertThatThrownBy(() -> gateway.verifyPayment(null, "txn_123", "test_valid_sig"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("strictly disabled in production");
    }
}
