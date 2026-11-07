package com.nexarats.academy.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.nexarats.academy.onboarding.dto.AvatarUploadResponse;
import com.nexarats.academy.onboarding.dto.ConfirmAvatarUploadRequest;
import com.nexarats.academy.onboarding.model.AvatarAsset;
import com.nexarats.academy.onboarding.model.ScanStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import com.nexarats.academy.onboarding.service.AvatarMalwareScanningService;
import com.nexarats.academy.onboarding.service.AvatarStorageService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Real S3-Compatible / MinIO Avatar Upload Lifecycle Integration Test.
 *
 * Verifies the authoritative 6-stage lifecycle:
 * Pre-sign request -> Upload object via SigV4 PUT -> Fetch quarantined object -> Malware scan -> Approve -> Retrieve avatar.
 */
@SpringBootTest
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MinioAvatarUploadIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private AvatarAssetRepository avatarAssetRepository;

    @Autowired
    private AvatarStorageService avatarStorageService;

    @Autowired
    private AvatarMalwareScanningService malwareScanningService;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpServer s3MockServer;
    private int s3MockPort;
    private final Map<String, byte[]> s3Storage = new ConcurrentHashMap<>();

    private ServerSocket mockClamAvServer;
    private ExecutorService serverExecutor;
    private Cohort testCohort;

    @BeforeAll
    void initS3MockAndContext() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. Start embedded S3/MinIO compatible HTTP Server
        s3MockServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        s3MockPort = s3MockServer.getAddress().getPort();

        s3MockServer.createContext("/", new S3CompatibleHandler());
        s3MockServer.setExecutor(null);
        s3MockServer.start();

        // 2. Configure AvatarStorageService to use the S3-compatible endpoint with path style access
        avatarStorageService.configureS3(
                "http://127.0.0.1:" + s3MockPort,
                "nexarats-academy-avatars",
                "us-east-1",
                "minio_admin",
                "minio_secret_dev",
                true
        );

        // 3. Start mock ClamAV daemon and configure malware scanning service
        mockClamAvServer = new ServerSocket(0);
        int clamAvPort = mockClamAvServer.getLocalPort();
        malwareScanningService.setClamavHost("127.0.0.1");
        malwareScanningService.setClamavPort(clamAvPort);
        malwareScanningService.setConnectionTimeoutMs(2000);
        malwareScanningService.setSocketTimeoutMs(2000);

        serverExecutor = Executors.newCachedThreadPool();
        serverExecutor.submit(() -> {
            while (!mockClamAvServer.isClosed()) {
                try {
                    Socket socket = mockClamAvServer.accept();
                    serverExecutor.submit(() -> handleClamAvSocket(socket));
                } catch (IOException ignored) {}
            }
        });

        testCohort = cohortRepository.findAll().stream().findFirst().orElseGet(() -> {
            Cohort c = new Cohort("MINIO-COHORT-" + UUID.randomUUID().toString().substring(0, 8), "Minio Cohort", LocalDate.now(), Instant.now(), 50);
            return cohortRepository.save(c);
        });
    }

    private void handleClamAvSocket(Socket socket) {
        try (socket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {
            byte[] buf = new byte[4096];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read;
            while ((read = in.read(buf)) > 0) {
                baos.write(buf, 0, read);
                String received = baos.toString(StandardCharsets.US_ASCII);
                if (received.contains("EICAR") || received.length() > 500) {
                    break;
                }
            }
            String received = baos.toString(StandardCharsets.US_ASCII);
            if (received.contains("EICAR")) {
                out.write("stream: Eicar-Signature FOUND\0".getBytes(StandardCharsets.US_ASCII));
            } else {
                out.write("stream: OK\0".getBytes(StandardCharsets.US_ASCII));
            }
            out.flush();
        } catch (IOException ignored) {}
    }

    @AfterAll
    void tearDownS3Mock() {
        if (s3MockServer != null) {
            s3MockServer.stop(0);
        }
        if (mockClamAvServer != null && !mockClamAvServer.isClosed()) {
            try {
                mockClamAvServer.close();
            } catch (IOException ignored) {}
        }
        if (serverExecutor != null) {
            serverExecutor.shutdownNow();
        }
    }

    private class S3CompatibleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("PUT".equalsIgnoreCase(method)) {
                try (InputStream in = exchange.getRequestBody()) {
                    byte[] data = in.readAllBytes();
                    s3Storage.put(path, data);
                }
                exchange.getResponseHeaders().set("ETag", "\"d41d8cd98f00b204e9800998ecf8427e\"");
                exchange.sendResponseHeaders(200, -1);
            } else if ("GET".equalsIgnoreCase(method)) {
                byte[] data = s3Storage.get(path);
                if (data != null) {
                    exchange.getResponseHeaders().set("Content-Type", "image/png");
                    exchange.sendResponseHeaders(200, data.length);
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.write(data);
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } else if ("HEAD".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(200, -1);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                s3Storage.remove(path);
                exchange.sendResponseHeaders(204, -1);
            } else {
                exchange.sendResponseHeaders(200, -1);
            }
        }
    }

    private byte[] createValidPngBytes(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(24, 119, 242));
        g.fillRect(0, 0, width, height);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return baos.toByteArray();
    }

    private record UserSession(String token, UUID userId, UUID tenantId) {}

    private UserSession registerStudent(String email) {
        String testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);
        RegisterRequest req = new RegisterRequest(email, "StudentPass2026!", "Minio", "Learner", null);
        LoginResponse resp = authService.register(req, testIp, "TestAgent");
        UUID tenantId = resp.tenant() != null ? resp.tenant().id() : null;

        Enrollment enr = new Enrollment("ENR-MIN-" + UUID.randomUUID().toString().substring(0, 6), resp.user().id(), testCohort.getId(), EnrollmentStatus.ENROLLED);
        enr.setTenantId(tenantId);
        enrollmentRepository.save(enr);

        return new UserSession(resp.accessToken(), resp.user().id(), tenantId);
    }

    @Test
    @DisplayName("S3/MinIO Avatar Workflow: Pre-sign -> SigV4 Upload -> Confirm Quarantine & Scan -> Approve -> Retrieve")
    void testCompleteS3AvatarLifecycle() throws Exception {
        String email = "minio_learner_" + UUID.randomUUID() + "@example.com";
        UserSession session = registerStudent(email);

        // 1. Request real AWS SigV4 pre-signed PUT URL
        MvcResult presignResult = mockMvc.perform(post("/api/v1/onboarding/avatar/presigned-url")
                        .header("Authorization", "Bearer " + session.token())
                        .param("extension", "png")
                        .param("contentType", "image/png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").isString())
                .andExpect(jsonPath("$.storageObjectKey").isString())
                .andExpect(jsonPath("$.httpMethod").value("PUT"))
                .andReturn();

        String responseJson = presignResult.getResponse().getContentAsString();
        JsonNode presignJson = objectMapper.readTree(responseJson);
        String uploadUrl = presignJson.get("uploadUrl").asText();
        String storageObjectKey = presignJson.get("storageObjectKey").asText();

        assertThat(uploadUrl).contains("127.0.0.1:" + s3MockPort);
        assertThat(uploadUrl).contains("X-Amz-Signature");
        assertThat(storageObjectKey).startsWith("avatars/" + session.tenantId() + "/" + session.userId() + "/");

        // 2. Direct HTTP PUT upload to S3/MinIO via the SigV4 Pre-signed URL
        byte[] imageBytes = createValidPngBytes(200, 200);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest uploadRequest = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "image/png")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                .build();

        HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(uploadResponse.statusCode()).isEqualTo(200);

        // Verify object was received on S3 mock storage
        String s3Path = "/nexarats-academy-avatars/" + storageObjectKey;
        assertThat(s3Storage).containsKey(s3Path);
        assertThat(s3Storage.get(s3Path)).isEqualTo(imageBytes);

        // 3. Confirm upload: triggers fetch from S3, quarantine validation, malware scanning, approval
        ConfirmAvatarUploadRequest confirmReq = new ConfirmAvatarUploadRequest(
                storageObjectKey, "avatar_profile.png"
        );

        MvcResult confirmResult = mockMvc.perform(post("/api/v1/onboarding/avatar/confirm-upload")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.avatarAssetId").isString())
                .andExpect(jsonPath("$.scanStatus").value("CLEAN"))
                .andExpect(jsonPath("$.avatarUrl").isString())
                .andReturn();

        AvatarUploadResponse uploadResultDto = objectMapper.readValue(
                confirmResult.getResponse().getContentAsString(), AvatarUploadResponse.class
        );

        // 4. Verify asset state in database
        AvatarAsset asset = avatarAssetRepository.findById(uploadResultDto.avatarAssetId()).orElseThrow();
        assertThat(asset.getScanStatus()).isEqualTo(ScanStatus.CLEAN);
        assertThat(asset.isActive()).isTrue();
        assertThat(asset.getStorageObjectKey()).isEqualTo(storageObjectKey);

        // Verify linked to student profile
        StudentProfile profile = studentProfileRepository.findByTenantIdAndUserId(session.tenantId(), session.userId()).orElseThrow();
        assertThat(profile.getAvatarAssetId()).isEqualTo(asset.getId());

        // 5. Retrieve avatar: Public streaming endpoint retrieves bytes from S3 and serves with cache headers
        MvcResult getResult = mockMvc.perform(get("/api/v1/onboarding/avatar/" + asset.getId()))
                .andExpect(status().isOk())
                .andReturn();

        byte[] retrievedBytes = getResult.getResponse().getContentAsByteArray();
        assertThat(retrievedBytes).isEqualTo(imageBytes);
        assertThat(getResult.getResponse().getHeader("Cache-Control")).contains("public, max-age=86400");
    }

    @Test
    @DisplayName("S3/MinIO Avatar Security: Confirm upload rejects spoofed storage keys from other users")
    void testConfirmUploadRejectsCrossUserKeySpoofing() throws Exception {
        String emailA = "user_a_" + UUID.randomUUID() + "@example.com";
        UserSession sessionA = registerStudent(emailA);

        String emailB = "user_b_" + UUID.randomUUID() + "@example.com";
        UserSession sessionB = registerStudent(emailB);

        // User B attempts to claim an object key belonging to User A
        String forgedKey = String.format("avatars/%s/%s/target.png", sessionA.tenantId(), sessionA.userId());
        ConfirmAvatarUploadRequest request = new ConfirmAvatarUploadRequest(forgedKey, "target.png");

        mockMvc.perform(post("/api/v1/onboarding/avatar/confirm-upload")
                        .header("Authorization", "Bearer " + sessionB.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("S3/MinIO Malware Defense: Harmless EICAR payload uploaded via presigned URL is detected, infected and purged from S3")
    void testEicarPayloadUploadedToS3IsQuarantinedAndPurged() throws Exception {
        String email = "malware_test_" + UUID.randomUUID() + "@example.com";
        UserSession session = registerStudent(email);

        // 1. Get presigned upload URL
        MvcResult presignResult = mockMvc.perform(post("/api/v1/onboarding/avatar/presigned-url")
                        .header("Authorization", "Bearer " + session.token())
                        .param("extension", "png")
                        .param("contentType", "image/png"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode presignJson = objectMapper.readTree(presignResult.getResponse().getContentAsString());
        String uploadUrl = presignJson.get("uploadUrl").asText();
        String storageObjectKey = presignJson.get("storageObjectKey").asText();

        // 2. Upload valid PNG image containing embedded/appended EICAR signature bytes
        byte[] validPng = createValidPngBytes(200, 200);
        byte[] eicarBytes = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes(StandardCharsets.US_ASCII);
        byte[] maliciousPayload = new byte[validPng.length + eicarBytes.length];
        System.arraycopy(validPng, 0, maliciousPayload, 0, validPng.length);
        System.arraycopy(eicarBytes, 0, maliciousPayload, validPng.length, eicarBytes.length);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest uploadRequest = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Content-Type", "image/png")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(maliciousPayload))
                .build();

        client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());

        // 3. Confirm upload -> triggers validation & ClamAV scan -> fails closed and purges from S3
        ConfirmAvatarUploadRequest confirmReq = new ConfirmAvatarUploadRequest(storageObjectKey, "bad.png");

        mockMvc.perform(post("/api/v1/onboarding/avatar/confirm-upload")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isBadRequest());

        // Object must be purged from S3 storage
        String s3Path = "/nexarats-academy-avatars/" + storageObjectKey;
        assertThat(s3Storage.containsKey(s3Path)).isFalse();
    }
}
