package com.nexarats.academy.onboarding;

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
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.RateLimitingService;
import com.nexarats.academy.onboarding.dto.AcceptAgreementRequest;
import com.nexarats.academy.onboarding.dto.UpdateProfileRequest;
import com.nexarats.academy.onboarding.model.LegalAgreement;
import com.nexarats.academy.onboarding.repository.LegalAgreementRepository;
import com.nexarats.academy.onboarding.service.AvatarMalwareScanningService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
public class StudentOnboardingIntegrationTests {

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
    private CohortRepository cohortRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LegalAgreementRepository legalAgreementRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private AvatarMalwareScanningService malwareScanningService;

    private Cohort testCohort;
    private ServerSocket mockClamAvServer;
    private ExecutorService serverExecutor;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingService.resetRateLimits();
        testCohort = cohortRepository.findAll().stream().findFirst().orElse(null);

        // Start mock ClamAV daemon socket fixture to verify live TCP stream scanning
        mockClamAvServer = new ServerSocket(0);
        int port = mockClamAvServer.getLocalPort();
        malwareScanningService.setClamavHost("127.0.0.1");
        malwareScanningService.setClamavPort(port);
        malwareScanningService.setConnectionTimeoutMs(2000);
        malwareScanningService.setSocketTimeoutMs(2000);

        serverExecutor = Executors.newCachedThreadPool();
        serverExecutor.submit(() -> {
            while (!mockClamAvServer.isClosed()) {
                try (Socket client = mockClamAvServer.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.US_ASCII));
                    OutputStream out = client.getOutputStream();
                    // Consume zINSTREAM command
                    char[] buf = new char[10];
                    int r = in.read(buf, 0, 10);
                    if (r > 0) {
                        out.write("stream: OK\n".getBytes(StandardCharsets.US_ASCII));
                        out.flush();
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockClamAvServer != null && !mockClamAvServer.isClosed()) {
            mockClamAvServer.close();
        }
        if (serverExecutor != null) {
            serverExecutor.shutdownNow();
        }
    }

    private UserAuth registerStudent(String email, String firstName, String lastName) {
        String testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);
        RegisterRequest req = new RegisterRequest(email, "StudentPass2026!", firstName, lastName, null);
        LoginResponse resp = authService.register(req, testIp, "TestAgent");
        return new UserAuth(resp.accessToken(), resp.user().id(), resp.tenant() != null ? resp.tenant().id() : null);
    }

    private record UserAuth(String token, UUID userId, UUID tenantId) {}

    private byte[] createTestJpegBytes() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("End-to-End: Complete Student Academic Onboarding Flow & Enrollment Activation")
    void testFullOnboardingWorkflow() throws Exception {
        String email = "onboard_student_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Priya", "Nair");

        // 1. Initial Status Check -> NOT_STARTED
        mockMvc.perform(get("/api/v1/onboarding/status")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.canActivate").value(false))
                .andExpect(jsonPath("$.profileCompleted").value(false));

        // 2. Update Student Profile
        UpdateProfileRequest profileReq = new UpdateProfileRequest(
                "Full Stack Developer | AI Systems",
                "Passionate about building scalable distributed systems and LLM workflows.",
                "B.Tech Computer Science",
                "Senior Backend Engineer at high-scale tech company",
                "Asia/Kolkata",
                "https://github.com/priyanair-nex",
                "https://linkedin.com/in/priyanair"
        );

        mockMvc.perform(put("/api/v1/onboarding/profile")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Full Stack Developer | AI Systems"))
                .andExpect(jsonPath("$.onboardingStatus").value("IN_PROGRESS"));

        // 3. Create an enrollment in ENROLLED status before completing onboarding
        Enrollment enrollment = null;
        if (testCohort != null) {
            enrollment = new Enrollment(
                    "ENR-ONB-" + UUID.randomUUID().toString().substring(0, 6),
                    auth.userId(),
                    testCohort.getId(),
                    EnrollmentStatus.ENROLLED
            );
            enrollment.setTenantId(auth.tenantId());
            enrollment = enrollmentRepository.save(enrollment);
        }

        // 4. Upload Valid Avatar Photo (scanned via mock ClamAV TCP socket)
        byte[] jpegBytes = createTestJpegBytes();
        MockMultipartFile file = new MockMultipartFile("file", "priya-avatar.jpg", "image/jpeg", jpegBytes);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/onboarding/avatar")
                        .file(file)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scanStatus").value("CLEAN"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.avatarUrl").isNotEmpty())
                .andReturn();

        String uploadJson = uploadResult.getResponse().getContentAsString();
        String avatarAssetId = objectMapper.readTree(uploadJson).get("avatarAssetId").asText();

        // 5. Verify Avatar Can Be Streamed Publicly with Cache Headers
        mockMvc.perform(get("/api/v1/onboarding/avatar/" + avatarAssetId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().exists("Cache-Control"));

        // 6. Fetch Legal Agreements & Sign All Active Ones
        mockMvc.perform(get("/api/v1/onboarding/agreements")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        List<LegalAgreement> agreements = legalAgreementRepository.findActiveAgreementsForTenant(auth.tenantId());
        assertThat(agreements).isNotEmpty();

        for (LegalAgreement ag : agreements) {
            AcceptAgreementRequest acceptReq = new AcceptAgreementRequest(ag.getVersion());
            mockMvc.perform(post("/api/v1/onboarding/agreements/" + ag.getId() + "/accept")
                            .header("Authorization", "Bearer " + auth.token())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(acceptReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isAccepted").value(true));
        }

        // 7. Check Onboarding Status -> Now canActivate is TRUE
        mockMvc.perform(get("/api/v1/onboarding/status")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canActivate").value(true))
                .andExpect(jsonPath("$.profileCompleted").value(true))
                .andExpect(jsonPath("$.developerLinksCompleted").value(true))
                .andExpect(jsonPath("$.avatarUploaded").value(true))
                .andExpect(jsonPath("$.legalAgreementsAccepted").value(true));

        // 8. Complete Onboarding Activation
        mockMvc.perform(post("/api/v1/onboarding/complete")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.onboardingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.enrollmentStatus").value("ACTIVE"));

        // 9. Verify Enrollment Transitioned to ACTIVE in Database
        if (enrollment != null) {
            Enrollment updated = enrollmentRepository.findById(enrollment.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        }

        // 10. Verify User Avatar URL Synchronized
        User user = userRepository.findById(auth.userId()).orElseThrow();
        assertThat(user.getAvatarUrl()).contains("/api/v1/onboarding/avatar/" + avatarAssetId);

        // 11. Verify Repeat Activation is Idempotent
        mockMvc.perform(post("/api/v1/onboarding/complete")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.enrollmentStatus").value("ACTIVE"));
    }

    @Test
    @DisplayName("Backend Access Control: Incomplete onboarding blocks video playback sessions")
    void testIncompleteOnboardingBlocksPlaybackSessions() throws Exception {
        String email = "blocked_learner_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Amit", "Kumar");

        // Set incomplete profile
        UpdateProfileRequest profileReq = new UpdateProfileRequest(
                "Frontend Dev", "Bio text", "B.Sc", "Goal", "UTC", "https://github.com/amit", null
        );
        mockMvc.perform(put("/api/v1/onboarding/profile")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk());

        // Create enrollment
        Enrollment enr = new Enrollment(
                "ENR-ONB-BLK-" + UUID.randomUUID().toString().substring(0, 6),
                auth.userId(),
                testCohort != null ? testCohort.getId() : UUID.randomUUID(),
                EnrollmentStatus.ENROLLED
        );
        enr.setTenantId(auth.tenantId());
        Course course = courseRepository.findAll().stream().findFirst().orElse(null);
        if (course != null) {
            enr.setCourseId(course.getId());
        }
        enrollmentRepository.save(enr);

        Lesson lesson = lessonRepository.findAll().stream().findFirst().orElse(null);
        if (lesson != null) {
            // Attempt to create playback session with incomplete onboarding -> 403 Forbidden
            mockMvc.perform(post("/api/v1/learning/lessons/" + lesson.getId() + "/playback-session")
                            .header("Authorization", "Bearer " + auth.token()))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Rejects onboarding completion when student has no enrollment in current tenant")
    void testRejectsCompletionWithoutEnrollment() throws Exception {
        String email = "no_enroll_student_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Siddharth", "Patel");

        UpdateProfileRequest profileReq = new UpdateProfileRequest(
                "Full Stack Developer", "Bio text", "B.Tech", "Engineer", "UTC", "https://github.com/sid", null
        );
        mockMvc.perform(put("/api/v1/onboarding/profile")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileReq)))
                .andExpect(status().isOk());

        // Attempt complete with NO enrollment -> 400 Bad Request
        mockMvc.perform(post("/api/v1/onboarding/complete")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rejects avatar upload with SVG/HTML script tag disguised as image")
    void testRejectsMaliciousSvgUpload() throws Exception {
        String email = "hacker_" + UUID.randomUUID() + "@example.com";
        UserAuth auth = registerStudent(email, "Eve", "Malicious");

        byte[] fakeSvg = "<svg><script>alert('pwned')</script></svg>".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "evil.jpg", "image/jpeg", fakeSvg);

        mockMvc.perform(multipart("/api/v1/onboarding/avatar")
                        .file(file)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Denies unauthenticated access to protected onboarding endpoints (401 Unauthorized)")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/status"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/onboarding/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/onboarding/complete"))
                .andExpect(status().isUnauthorized());
    }
}
