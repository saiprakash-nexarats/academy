package com.nexarats.academy.identity;

import com.nexarats.academy.common.security.JwtTokenService;
import com.nexarats.academy.identity.dto.LoginRequest;
import com.nexarats.academy.identity.dto.LoginResponse;
import com.nexarats.academy.identity.dto.RegisterRequest;
import com.nexarats.academy.identity.dto.TokenRefreshResponse;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.SessionService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
class JwtAndRefreshTokenIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private com.nexarats.academy.identity.service.RateLimitingService rateLimitingService;

    private String testEmail;
    private String rawPassword;
    private String testIp;

    @BeforeEach
    void setUp() {
        rateLimitingService.resetRateLimits();
        testEmail = "token.test." + UUID.randomUUID().toString().substring(0, 8) + "@nexarats.academy";
        rawPassword = "TokenPassSecure2026!";
        testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);
    }

    @Test
    @DisplayName("JWT Access Tokens: Issues RS256 signed tokens with all mandatory enterprise claims")
    void testJwtIssuanceAndClaims() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Kavya", "Sharma", null);
        LoginResponse loginResp = authService.register(registerReq, testIp, "TestAgent");

        String jwt = loginResp.accessToken();
        assertThat(jwt).isNotBlank();

        // Validate and parse token cryptographically
        Claims claims = jwtTokenService.validateAndParseToken(jwt);
        assertThat(claims.getSubject()).isEqualTo(loginResp.user().id().toString());
        assertThat(claims.get("email")).isEqualTo(testEmail);
        assertThat(claims.get("tenant_id")).isEqualTo(loginResp.tenant().id().toString());
        assertThat(claims.get("tenant_slug")).isEqualTo(loginResp.tenant().slug());
        assertThat(claims.get("sid")).isNotNull();
        assertThat(claims.getIssuer()).isEqualTo("nexarats-academy");
        assertThat(claims.getAudience()).contains("nexarats-clients");

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles", List.class);
        assertThat(roles).contains("ROLE_STUDENT");
    }

    @Test
    @DisplayName("JWT Validation: Tampered token signature is cryptographically rejected")
    void testTamperedTokenRejected() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Arjun", "Reddy", null);
        LoginResponse loginResp = authService.register(registerReq, testIp, "TestAgent");

        String validJwt = loginResp.accessToken();
        // Tamper with the payload part of the JWT
        String[] parts = validJwt.split("\\.");
        assertThat(parts).hasSize(3);
        String tamperedJwt = parts[0] + "." + parts[1] + "tampered" + "." + parts[2];

        assertThatThrownBy(() -> jwtTokenService.validateAndParseToken(tamperedJwt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Refresh Token Rotation: Rotates token on every use and issues new tokens")
    void testRefreshTokenRotation() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Dev", "Anand", null);
        LoginResponse loginResp = authService.register(registerReq, testIp, "TestAgent");

        String originalRefreshToken = loginResp.refreshToken();
        assertThat(originalRefreshToken).isNotBlank();

        // 1. Refresh using original token
        TokenRefreshResponse firstRefresh = authService.refresh(originalRefreshToken, testIp, "TestAgent");
        assertThat(firstRefresh.accessToken()).isNotBlank();
        assertThat(firstRefresh.refreshToken()).isNotBlank();
        assertThat(firstRefresh.refreshToken()).isNotEqualTo(originalRefreshToken);

        // 2. Refresh using the newly rotated token
        TokenRefreshResponse secondRefresh = authService.refresh(firstRefresh.refreshToken(), testIp, "TestAgent");
        assertThat(secondRefresh.accessToken()).isNotBlank();
        assertThat(secondRefresh.refreshToken()).isNotBlank();
        assertThat(secondRefresh.refreshToken()).isNotEqualTo(firstRefresh.refreshToken());
    }

    @Test
    @DisplayName("Refresh Token Security: Replay / reuse of superseded token triggers full session compromise revocation")
    void testRefreshTokenReplayDetection() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Sneha", "Gupta", null);
        LoginResponse loginResp = authService.register(registerReq, testIp, "TestAgent");

        String token1 = loginResp.refreshToken();

        // 1. Legitimate rotation: token1 -> token2
        TokenRefreshResponse rotated = authService.refresh(token1, testIp, "TestAgent");
        String token2 = rotated.refreshToken();

        // 2. Attacker replays token1 (already used/revoked)
        assertThatThrownBy(() -> authService.refresh(token1, testIp, "AttackerAgent"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("token reuse");

        // 3. Verify breach defense: Even token2 must now be invalidated because user's sessions were purged!
        assertThatThrownBy(() -> authService.refresh(token2, testIp, "TestAgent"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Logout Revocation: Logout terminates active session and blocks token refresh")
    void testLogoutRevocation() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Tara", "Singh", null);
        LoginResponse loginResp = authService.register(registerReq, testIp, "TestAgent");

        String refreshToken = loginResp.refreshToken();
        Claims claims = jwtTokenService.validateAndParseToken(loginResp.accessToken());
        UUID sessionId = UUID.fromString(claims.get("sid", String.class));

        // Active before logout
        assertThat(sessionService.isSessionActive(sessionId)).isTrue();

        // Logout
        authService.logout(sessionId, loginResp.user().id(), testIp, "TestAgent");

        // Inactive after logout
        assertThat(sessionService.isSessionActive(sessionId)).isFalse();

        // Refresh attempt must fail
        assertThatThrownBy(() -> authService.refresh(refreshToken, testIp, "TestAgent"))
                .isInstanceOf(Exception.class);
    }
}
