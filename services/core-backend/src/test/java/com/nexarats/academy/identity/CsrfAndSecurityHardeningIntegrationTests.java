package com.nexarats.academy.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexarats.academy.identity.dto.BootstrapAdminRequest;
import com.nexarats.academy.identity.dto.LoginRequest;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.dto.TokenRefreshRequest;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.RateLimitingService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Rigorous Security Hardening Integration Tests.
 *
 * Verifies Task 4 requirements:
 * 1. Double-submit CSRF token protection on cookie-authenticated refresh and logout.
 * 2. Rejection of arbitrary X-Requested-With header without matching CSRF token.
 * 3. Permitted body-authenticated refresh for mobile/API clients.
 * 4. Redis rate-limiting fail-closed strategy policy.
 * 5. Administrator bootstrap endpoint access controls and invalid secret rejection.
 */
@SpringBootTest
@ActiveProfiles("local")
class CsrfAndSecurityHardeningIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimitingService rateLimitingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String testEmail;
    private String rawPassword;
    private String testIp;

    @BeforeEach
    void setUp() {
        rateLimitingService.resetRateLimits();
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testEmail = "csrf.test." + suffix + "@nexarats.academy";
        rawPassword = "CsrfPassword2026!";
        testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);
    }

    @Test
    @DisplayName("CSRF: Cookie-authenticated refresh without CSRF token header is rejected with 403 Forbidden")
    void testCookieRefreshWithoutCsrfHeaderRejected() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Aarav", "Patel", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");

        Cookie refreshCookie = new Cookie("nexarats_refresh_token", registered.refreshToken());
        Cookie csrfCookie = new Cookie("nexarats_csrf_token", UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie, csrfCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF: X-Requested-With header ALONE cannot bypass CSRF token verification")
    void testXRequestedWithCannotBypassCsrf() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Rohan", "Verma", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");

        Cookie refreshCookie = new Cookie("nexarats_refresh_token", registered.refreshToken());
        Cookie csrfCookie = new Cookie("nexarats_csrf_token", UUID.randomUUID().toString());

        // Attempting bypass with X-Requested-With: XMLHttpRequest must fail with 403
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie, csrfCookie)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF: Cookie-authenticated refresh with matching CSRF token succeeds with 200 OK")
    void testCookieRefreshWithValidMatchingCsrfTokenSucceeds() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Priya", "Nair", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");

        String validCsrfToken = UUID.randomUUID().toString();
        Cookie refreshCookie = new Cookie("nexarats_refresh_token", registered.refreshToken());
        Cookie csrfCookie = new Cookie("nexarats_csrf_token", validCsrfToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie, csrfCookie)
                        .header("X-CSRF-Token", validCsrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("CSRF: Cookie-authenticated logout without matching CSRF token is rejected with 403 Forbidden")
    void testCookieLogoutWithoutCsrfRejected() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Vikram", "Mehta", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");

        Cookie refreshCookie = new Cookie("nexarats_refresh_token", registered.refreshToken());
        Cookie csrfCookie = new Cookie("nexarats_csrf_token", UUID.randomUUID().toString());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshCookie, csrfCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF: Cookie-authenticated logout with matching CSRF token succeeds with 200 OK")
    void testCookieLogoutWithValidCsrfSucceeds() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Vikram", "Mehta", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");

        String validCsrfToken = UUID.randomUUID().toString();
        Cookie refreshCookie = new Cookie("nexarats_refresh_token", registered.refreshToken());
        Cookie csrfCookie = new Cookie("nexarats_csrf_token", validCsrfToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshCookie, csrfCookie)
                        .header("X-CSRF-Token", validCsrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    @DisplayName("Mobile/API Session: Body-authenticated refresh succeeds without ambient cookies")
    void testBodyAuthenticatedRefreshSucceedsWithoutCookies() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Ananya", "Deshmukh", null);
        LoginResponse registered = authService.register(registerReq, testIp, "TestAgent");

        TokenRefreshRequest bodyReq = new TokenRefreshRequest(registered.refreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Rate Limiting: Fail-closed policy rejects requests when Redis encounters exceptions")
    void testRateLimitingFailClosedPolicy() {
        StringRedisTemplate brokenRedis = mock(StringRedisTemplate.class);
        when(brokenRedis.opsForValue()).thenThrow(new RuntimeException("Simulated Redis Connection Failure"));

        RateLimitingService failClosedLimiter = new RateLimitingService(brokenRedis, 5, "fail-closed");
        boolean allowed = failClosedLimiter.checkLoginAllowed("192.168.1.100", "test.user@failclosed.org");

        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("Bootstrap Admin: Request with invalid secret is rejected with 403 Forbidden")
    void testBootstrapAdminWithInvalidSecretRejected() throws Exception {
        BootstrapAdminRequest request = new BootstrapAdminRequest(
                "invalid-secret-xyz",
                "admin.fail@alpha.corp",
                "SecureAdminPass2026!",
                "Super",
                "Admin",
                "alpha-corp",
                "Alpha Corporation"
        );

        mockMvc.perform(post("/api/v1/auth/bootstrap-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Bootstrap Admin: Request with valid secret in development environment provisions administrator")
    void testBootstrapAdminWithValidSecretSucceeds() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        BootstrapAdminRequest request = new BootstrapAdminRequest(
                "dev-bootstrap-secret-2026",
                "admin." + uniqueSuffix + "@nexarats.academy",
                "SecureAdminPass2026!",
                "Privileged",
                "Admin",
                "tenant-" + uniqueSuffix,
                "Tenant " + uniqueSuffix
        );

        mockMvc.perform(post("/api/v1/auth/bootstrap-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.roles").isArray());
    }
}
