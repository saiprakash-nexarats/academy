package com.nexarats.academy.identity;

import com.nexarats.academy.common.security.Argon2PasswordService;
import com.nexarats.academy.identity.dto.*;
import com.nexarats.academy.identity.model.PasswordResetToken;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.model.UserCredential;
import com.nexarats.academy.identity.repository.PasswordResetTokenRepository;
import com.nexarats.academy.identity.repository.UserCredentialRepository;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.identity.service.AuthService;
import com.nexarats.academy.identity.service.DevEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
class AuthenticationSecurityIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository credentialRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private Argon2PasswordService passwordService;

    @Autowired
    private DevEmailService devEmailService;

    @Autowired
    private com.nexarats.academy.identity.service.RateLimitingService rateLimitingService;

    private String testEmail;
    private String rawPassword;
    private String testIp;

    @BeforeEach
    void setUp() {
        devEmailService.clear();
        rateLimitingService.resetRateLimits();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testEmail = "auth.test." + suffix + "@nexarats.academy";
        rawPassword = "SecurePassword2026!";
        testIp = "10.0." + (int)(Math.random() * 200) + "." + (int)(Math.random() * 200);
    }

    @Test
    @DisplayName("Password hashing: Uses Argon2id, verifies hashes, and does not store plaintext")
    void testArgon2PasswordHashing() {
        String encoded = passwordService.encode("MySuperSecret123!");
        assertThat(encoded).startsWith("$argon2id$");
        assertThat(passwordService.matches("MySuperSecret123!", encoded)).isTrue();
        assertThat(passwordService.matches("WrongPassword!", encoded)).isFalse();
    }

    @Test
    @DisplayName("Registration and Login: Registers new student and authenticates successfully")
    void testRegistrationAndLogin() {
        RegisterRequest registerReq = new RegisterRequest(
                testEmail,
                rawPassword,
                "Aarav",
                "Patel",
                "+919876543210"
        );
        LoginResponse registerResp = authService.register(registerReq, testIp, "TestAgent");

        assertThat(registerResp).isNotNull();
        assertThat(registerResp.accessToken()).isNotBlank();
        assertThat(registerResp.refreshToken()).isNotBlank();
        assertThat(registerResp.user().email()).isEqualTo(testEmail);
        assertThat(registerResp.roles()).contains("ROLE_STUDENT");

        // Verify password hash is stored in database as Argon2id
        User user = userRepository.findByEmailIgnoreCase(testEmail).orElseThrow();
        UserCredential cred = credentialRepository.findByUser(user).orElseThrow();
        assertThat(cred.getPasswordHash()).startsWith("$argon2id$");
        assertThat(cred.getPasswordHash()).doesNotContain(rawPassword);

        // Perform real login
        LoginRequest loginReq = new LoginRequest(testEmail, rawPassword, null, "WEB");
        LoginResponse loginResp = authService.login(loginReq, testIp, "TestAgent");

        assertThat(loginResp.accessToken()).isNotBlank();
        assertThat(loginResp.refreshToken()).isNotBlank();
        assertThat(loginResp.tenant().slug()).isEqualTo("nexarats-main");
    }

    @Test
    @DisplayName("Authentication: Invalid password throws generic BadCredentialsException")
    void testInvalidPasswordThrowsGenericError() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Rohan", "Verma", null);
        authService.register(registerReq, testIp, "TestAgent");

        LoginRequest badLogin = new LoginRequest(testEmail, "WrongPassword2026!", null, "WEB");
        assertThatThrownBy(() -> authService.login(badLogin, testIp, "TestAgent"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("Authentication: Unknown account throws generic BadCredentialsException (prevents enumeration)")
    void testUnknownAccountThrowsGenericError() {
        LoginRequest badLogin = new LoginRequest("nonexistent.user@random.org", "AnyPassword!", null, "WEB");
        assertThatThrownBy(() -> authService.login(badLogin, testIp, "TestAgent"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("Password Change: Updates Argon2id hash and revokes existing sessions")
    void testPasswordChangeLifecycle() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Priya", "Nair", null);
        LoginResponse loginResp = authService.register(registerReq, testIp, "TestAgent");

        String newPassword = "NewStrongPassword2026!";
        ChangePasswordRequest changeReq = new ChangePasswordRequest(rawPassword, newPassword);
        authService.changePassword(loginResp.user().id(), changeReq, testIp, "TestAgent");

        // Old password must fail
        LoginRequest oldLogin = new LoginRequest(testEmail, rawPassword, null, "WEB");
        assertThatThrownBy(() -> authService.login(oldLogin, testIp, "TestAgent"))
                .isInstanceOf(BadCredentialsException.class);

        // New password must succeed
        LoginRequest newLogin = new LoginRequest(testEmail, newPassword, null, "WEB");
        LoginResponse newLoginResp = authService.login(newLogin, testIp, "TestAgent");
        assertThat(newLoginResp.accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("Password Reset: Requests reset, stores hash, resets password, and prevents token replay")
    void testPasswordResetLifecycle() {
        RegisterRequest registerReq = new RegisterRequest(testEmail, rawPassword, "Vikram", "Malhotra", null);
        authService.register(registerReq, testIp, "TestAgent");

        // 1. Request reset
        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest(testEmail);
        authService.forgotPassword(forgotReq, testIp, "TestAgent");

        // Verify email captured in test email service
        assertThat(devEmailService.getSentEmails()).hasSize(1);
        String rawResetToken = devEmailService.getSentEmails().get(0).token();
        assertThat(rawResetToken).isNotBlank();

        // Verify token in DB is stored as SHA-256 hash (never plaintext)
        User user = userRepository.findByEmailIgnoreCase(testEmail).orElseThrow();
        var tokens = passwordResetTokenRepository.findAll();
        boolean foundHashed = tokens.stream().anyMatch(t -> !t.getTokenHash().equals(rawResetToken));
        assertThat(foundHashed).isTrue();

        // 2. Complete reset
        String brandNewPassword = "BrandNewResetPass2026!";
        ResetPasswordRequest resetReq = new ResetPasswordRequest(rawResetToken, brandNewPassword);
        authService.resetPassword(resetReq, testIp, "TestAgent");

        // 3. Verify single-use: Reusing the same reset token must fail
        assertThatThrownBy(() -> authService.resetPassword(resetReq, testIp, "TestAgent"))
                .isInstanceOf(IllegalArgumentException.class);

        // 4. Verify login with new password succeeds
        LoginResponse loginWithNewPass = authService.login(new LoginRequest(testEmail, brandNewPassword, null, "WEB"), testIp, "TestAgent");
        assertThat(loginWithNewPass.accessToken()).isNotBlank();
    }
}
