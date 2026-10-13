package com.nexarats.academy.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.common.security.JwtAuthenticationFilter;
import com.nexarats.academy.identity.dto.BootstrapAdminRequest;
import com.nexarats.academy.identity.dto.LoginRequest;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
class RbacAuthorizationIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthService authService;

    @Autowired
    private com.nexarats.academy.identity.service.RateLimitingService rateLimitingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String studentToken;
    private String adminToken;
    private String testIp;

    @BeforeEach
    void setUp() {
        rateLimitingService.resetRateLimits();
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);

        // 1. Create and log in a regular student
        String studentEmail = "student." + suffix + "@nexarats.academy";
        RegisterRequest regReq = new RegisterRequest(studentEmail, "StudentPass2026!", "Ananya", "Sharma", null);
        LoginResponse studentLogin = authService.register(regReq, testIp, "TestAgent");
        studentToken = studentLogin.accessToken();

        // 2. Bootstrap an enterprise administrator
        String adminEmail = "admin." + suffix + "@nexarats.academy";
        BootstrapAdminRequest bootReq = new BootstrapAdminRequest(
                "dev-bootstrap-secret-2026",
                adminEmail,
                "SuperAdminPass2026!",
                "Vikram",
                "Aditya",
                "nexarats-main",
                "Nexarats Academy"
        );
        authService.bootstrapAdmin(bootReq, testIp, "TestAgent");

        LoginRequest adminLoginReq = new LoginRequest(adminEmail, "SuperAdminPass2026!", "nexarats-main", "WEB");
        LoginResponse adminLogin = authService.login(adminLoginReq, testIp, "TestAgent");
        adminToken = adminLogin.accessToken();
    }

    @Test
    @DisplayName("RBAC: Student can access student endpoint, but is denied admin operations with HTTP 403")
    void testStudentDeniedAdminEndpoint() throws Exception {
        // Student accessing student-only route -> 200 OK
        mockMvc.perform(get("/api/v1/test/student-only")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value("GRANTED_STUDENT"));

        // Student accessing admin-only route -> 403 Forbidden (RFC 7807)
        mockMvc.perform(get("/api/v1/test/admin-only")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("RBAC: Academy Admin accesses admin endpoint successfully")
    void testAdminAccessGranted() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin-only")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access").value("GRANTED_ADMIN"));
    }

    @Test
    @DisplayName("Unauthenticated: Protected endpoint access without token returns HTTP 401 Problem Detail")
    void testUnauthenticatedAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/test/student-only"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Bootstrap Security: Invalid bootstrap secret is rejected with AccessDeniedException")
    void testBootstrapWithInvalidSecretFails() {
        BootstrapAdminRequest bootReq = new BootstrapAdminRequest(
                "wrong-secret-attack",
                "fake.admin@attack.org",
                "AttackerPass2026!",
                "Fake",
                "Admin",
                "hacked-tenant",
                "Hacked Tenant"
        );
        assertThatThrownBy(() -> authService.bootstrapAdmin(bootReq, testIp, "TestAgent"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Invalid administrator bootstrap secret");
    }

    @Test
    @DisplayName("REST API: Live /api/v1/auth/me returns authenticated identity profile")
    void testGetProfileEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").isString())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.tenant.slug").value("nexarats-main"));
    }
}
