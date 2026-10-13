package com.nexarats.academy.identity.service;

import com.nexarats.academy.common.security.Argon2PasswordService;
import com.nexarats.academy.common.security.JwtTokenService;
import com.nexarats.academy.identity.dto.*;
import com.nexarats.academy.identity.model.*;
import com.nexarats.academy.identity.repository.*;
import com.nexarats.academy.tenant.Tenant;
import com.nexarats.academy.tenant.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Enterprise Authentication & Identity Service.
 *
 * Implements Tasks 3, 4, 5, 6, 7, 8:
 * - Argon2id credential verification with lockout protection.
 * - RS256 JWT access token issuance.
 * - Opaque refresh token rotation with single-use semantics and reuse detection.
 * - Multi-tenant context binding and membership verification.
 * - RBAC role and permission mapping.
 * - Secure password reset and verification lifecycles.
 * - Operator administrator bootstrap.
 */
@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final TenantMembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final TenantService tenantService;
    private final SessionService sessionService;
    private final JwtTokenService jwtTokenService;
    private final Argon2PasswordService passwordService;
    private final RateLimitingService rateLimitingService;
    private final AuditService auditService;
    private final EmailService emailService;

    @Value("${nexarats.auth.bootstrap-secret:dev-bootstrap-secret-2026}")
    private String configuredBootstrapSecret;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            UserCredentialRepository credentialRepository,
            TenantMembershipRepository membershipRepository,
            RoleRepository roleRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            TenantService tenantService,
            SessionService sessionService,
            JwtTokenService jwtTokenService,
            Argon2PasswordService passwordService,
            RateLimitingService rateLimitingService,
            AuditService auditService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.tenantService = tenantService;
        this.sessionService = sessionService;
        this.jwtTokenService = jwtTokenService;
        this.passwordService = passwordService;
        this.rateLimitingService = rateLimitingService;
        this.auditService = auditService;
        this.emailService = emailService;
    }

    /**
     * Authenticates a user with email and password.
     */
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.email() != null ? request.email().trim().toLowerCase() : "";

        // 1. Rate limiting check
        if (!rateLimitingService.checkLoginAllowed(ipAddress, email)) {
            auditService.recordEvent("LOGIN_RATE_LIMITED", null, email, null, ipAddress, userAgent, "BLOCKED", "Too many failed login attempts");
            throw new IllegalStateException("Too many login attempts. Please try again later.");
        }

        // 2. User lookup (Generic error to prevent account enumeration)
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            auditService.recordEvent("LOGIN_FAILURE", null, email, null, ipAddress, userAgent, "FAILURE", "User account not found");
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userOpt.get();

        // 3. Account status checks
        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            auditService.recordEvent("LOGIN_FAILURE", user.getId(), email, null, ipAddress, userAgent, "BLOCKED", "Account is locked");
            throw new BadCredentialsException("Account is locked. Please contact support.");
        }
        if ("DEACTIVATED".equalsIgnoreCase(user.getStatus())) {
            auditService.recordEvent("LOGIN_FAILURE", user.getId(), email, null, ipAddress, userAgent, "BLOCKED", "Account is deactivated");
            throw new BadCredentialsException("Account is deactivated.");
        }

        // 4. Credential lookup & lock verification
        UserCredential credential = credentialRepository.findByUser(user)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(Instant.now())) {
            auditService.recordEvent("LOGIN_FAILURE", user.getId(), email, null, ipAddress, userAgent, "BLOCKED", "Account temporarily locked until " + credential.getLockedUntil());
            throw new BadCredentialsException("Account is temporarily locked due to repeated failed logins. Please try again later.");
        }

        // 5. Password verification (Argon2id)
        if (!passwordService.matches(request.password(), credential.getPasswordHash())) {
            int failed = credential.getFailedLoginAttempts() + 1;
            credential.setFailedLoginAttempts(failed);
            if (failed >= 5) {
                credential.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
                log.warn("Account {} locked for 15 minutes after 5 failed login attempts", email);
            }
            credentialRepository.save(credential);

            auditService.recordEvent("LOGIN_FAILURE", user.getId(), email, null, ipAddress, userAgent, "FAILURE", "Invalid password attempt " + failed);
            throw new BadCredentialsException("Invalid email or password");
        }

        // 6. Reset failed attempts and update last login
        credential.setFailedLoginAttempts(0);
        credential.setLockedUntil(null);
        credentialRepository.save(credential);

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // 7. Resolve Tenant Context
        Tenant targetTenant = resolveTenantForUser(user, request.tenantSlug());

        // 8. Resolve Roles & Permissions in the active tenant
        TenantMembership membership = membershipRepository.findByTenantAndUser(targetTenant, user)
                .orElseThrow(() -> new AccessDeniedException("User is not an active member of tenant: " + targetTenant.getSlug()));

        if (!"ACTIVE".equalsIgnoreCase(membership.getStatus())) {
            auditService.recordEvent("LOGIN_FAILURE", user.getId(), email, targetTenant.getId(), ipAddress, userAgent, "BLOCKED", "Tenant membership status: " + membership.getStatus());
            throw new AccessDeniedException("Membership in tenant is suspended or inactive");
        }

        List<String> roleCodes = membership.getRoles().stream()
                .map(Role::getCode)
                .toList();

        List<String> permissions = membership.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .toList();

        // 9. Create Session & Opaque Refresh Token
        SessionService.RotatedTokenPair tokenPair = sessionService.createSession(
                user,
                targetTenant,
                ipAddress,
                userAgent,
                request.clientType() != null ? request.clientType() : "WEB"
        );

        // 10. Issue Asymmetric RS256 JWT Access Token
        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                targetTenant.getId(),
                targetTenant.getSlug(),
                tokenPair.session().getId(),
                roleCodes,
                permissions
        );

        auditService.recordEvent("LOGIN_SUCCESS", user.getId(), email, targetTenant.getId(), ipAddress, userAgent, "SUCCESS", null);

        UserSummaryDto userSummary = new UserSummaryDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getAvatarUrl());
        TenantSummaryDto tenantSummary = new TenantSummaryDto(targetTenant.getId(), targetTenant.getSlug(), targetTenant.getName(), targetTenant.getStatus());

        return new LoginResponse(
                accessToken,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                userSummary,
                tenantSummary,
                roleCodes,
                permissions,
                tokenPair.newRawRefreshToken()
        );
    }

    /**
     * Refreshes access token and rotates opaque refresh token.
     */
    public TokenRefreshResponse refresh(String rawRefreshToken, String ipAddress, String userAgent) {
        SessionService.RotatedTokenPair rotated = sessionService.rotateRefreshToken(rawRefreshToken, ipAddress, userAgent);
        UserSession session = rotated.session();
        User user = session.getUser();
        Tenant tenant = session.getTenant();

        TenantMembership membership = membershipRepository.findByTenantAndUser(tenant, user)
                .orElseThrow(() -> new AccessDeniedException("Membership no longer valid"));

        List<String> roleCodes = membership.getRoles().stream().map(Role::getCode).toList();
        List<String> permissions = membership.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getCode).distinct().toList();

        String newAccessToken = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                tenant.getId(),
                tenant.getSlug(),
                session.getId(),
                roleCodes,
                permissions
        );

        auditService.recordEvent("TOKEN_REFRESH", user.getId(), user.getEmail(), tenant.getId(), ipAddress, userAgent, "SUCCESS", null);

        return new TokenRefreshResponse(
                newAccessToken,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                rotated.newRawRefreshToken()
        );
    }

    /**
     * Logs out a session and revokes refresh tokens.
     */
    public void logout(UUID sessionId, UUID userId, String ipAddress, String userAgent) {
        sessionService.revokeSession(sessionId);
        auditService.recordEvent("LOGOUT", userId, null, null, ipAddress, userAgent, "SUCCESS", "Session revoked");
    }

    /**
     * Logs out using a raw refresh token (e.g. cookie-based logout without active bearer token).
     */
    public void logoutByRefreshToken(String rawRefreshToken, String ipAddress, String userAgent) {
        if (!org.springframework.util.StringUtils.hasText(rawRefreshToken)) return;
        sessionService.revokeSessionByRefreshToken(rawRefreshToken);
        auditService.recordEvent("LOGOUT", null, null, null, ipAddress, userAgent, "SUCCESS", "Session revoked via refresh token");
    }

    /**
     * Fetches authenticated user profile, active tenant, roles, and accessible memberships.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId, UUID currentTenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<TenantMembership> memberships = membershipRepository.findByUserId(userId);

        Tenant activeTenant = null;
        List<String> roles = List.of();
        List<String> permissions = List.of();

        for (TenantMembership tm : memberships) {
            if (tm.getTenant().getId().equals(currentTenantId)) {
                activeTenant = tm.getTenant();
                roles = tm.getRoles().stream().map(Role::getCode).toList();
                permissions = tm.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getCode).distinct().toList();
                break;
            }
        }

        if (activeTenant == null && !memberships.isEmpty()) {
            TenantMembership first = memberships.get(0);
            activeTenant = first.getTenant();
            roles = first.getRoles().stream().map(Role::getCode).toList();
            permissions = first.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getCode).distinct().toList();
        }

        TenantSummaryDto tenantSummary = activeTenant != null
                ? new TenantSummaryDto(activeTenant.getId(), activeTenant.getSlug(), activeTenant.getName(), activeTenant.getStatus())
                : null;

        List<TenantSummaryDto> availableTenants = memberships.stream()
                .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus()))
                .map(m -> new TenantSummaryDto(m.getTenant().getId(), m.getTenant().getSlug(), m.getTenant().getName(), m.getTenant().getStatus()))
                .toList();

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.isEmailVerified(),
                tenantSummary,
                roles,
                permissions,
                availableTenants
        );
    }

    /**
     * Changes user password, verifies current password, and invalidates all existing sessions.
     */
    public void changePassword(UUID userId, ChangePasswordRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserCredential credential = credentialRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Credentials not found"));

        if (!passwordService.matches(request.currentPassword(), credential.getPasswordHash())) {
            auditService.recordEvent("PASSWORD_CHANGE_FAILURE", userId, user.getEmail(), null, ipAddress, userAgent, "FAILURE", "Current password incorrect");
            throw new BadCredentialsException("Current password is not correct");
        }

        passwordService.validatePasswordPolicy(request.newPassword());
        String newHash = passwordService.encode(request.newPassword());

        credential.setPasswordHash(newHash);
        credentialRepository.save(credential);

        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        // Security requirement: Revoke all existing sessions
        sessionService.revokeAllSessionsForUser(userId);

        auditService.recordEvent("PASSWORD_CHANGED", userId, user.getEmail(), null, ipAddress, userAgent, "SUCCESS", "All previous sessions revoked");
    }

    /**
     * Requests a password reset. Uniform response prevents account enumeration.
     */
    public void forgotPassword(ForgotPasswordRequest request, String ipAddress, String userAgent) {
        String email = request.email().trim().toLowerCase();

        if (!rateLimitingService.checkPasswordResetAllowed(email)) {
            auditService.recordEvent("PASSWORD_RESET_RATE_LIMITED", null, email, null, ipAddress, userAgent, "BLOCKED", "Rate limit exceeded");
            return; // Uniform silent return
        }

        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            passwordResetTokenRepository.invalidateAllForUser(user);

            byte[] tokenBytes = new byte[32];
            secureRandom.nextBytes(tokenBytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            String tokenHash = sha256(rawToken);

            Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
            PasswordResetToken resetToken = new PasswordResetToken(user, tokenHash, expiresAt);
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), rawToken, user.getFirstName());
            auditService.recordEvent("PASSWORD_RESET_REQUESTED", user.getId(), user.getEmail(), null, ipAddress, userAgent, "SUCCESS", null);
        });
    }

    /**
     * Resets password using a validated single-use token.
     */
    public void resetPassword(ResetPasswordRequest request, String ipAddress, String userAgent) {
        passwordService.validatePasswordPolicy(request.newPassword());
        String tokenHash = sha256(request.token().trim());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("Password reset token is expired or has already been used");
        }

        User user = resetToken.getUser();
        UserCredential credential = credentialRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Credentials missing"));

        credential.setPasswordHash(passwordService.encode(request.newPassword()));
        credentialRepository.save(credential);

        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);

        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        sessionService.revokeAllSessionsForUser(user.getId());
        auditService.recordEvent("PASSWORD_RESET_COMPLETED", user.getId(), user.getEmail(), null, ipAddress, userAgent, "SUCCESS", "Password reset successfully; sessions revoked");
    }

    /**
     * Registers a new student account in the default academy tenant.
     */
    public LoginResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        String email = request.email().trim().toLowerCase();

        if (!rateLimitingService.checkRegistrationAllowed(ipAddress)) {
            throw new IllegalStateException("Registration rate limit exceeded. Please try again later.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email address already exists");
        }

        passwordService.validatePasswordPolicy(request.password());

        User user = new User(email, request.firstName().trim(), request.lastName().trim());
        user.setPhoneNumber(request.phoneNumber());
        user = userRepository.save(user);

        UserCredential credential = new UserCredential(user, passwordService.encode(request.password()));
        credentialRepository.save(credential);

        Tenant tenant = tenantService.getOrCreateTenant("nexarats-main", "Nexarats Academy");

        TenantMembership membership = new TenantMembership(tenant, user, "ACTIVE");
        Role studentRole = roleRepository.findByCode("ROLE_STUDENT")
                .orElseThrow(() -> new IllegalStateException("System role ROLE_STUDENT is not seeded"));
        membership.addRole(studentRole);
        membershipRepository.save(membership);

        auditService.recordEvent("USER_REGISTERED", user.getId(), email, tenant.getId(), ipAddress, userAgent, "SUCCESS", null);

        // Immediately establish session
        return login(new LoginRequest(email, request.password(), tenant.getSlug(), "WEB"), ipAddress, userAgent);
    }

    /**
     * Allows a multi-tenant user to switch their active tenant to another authorized tenant.
     */
    public LoginResponse switchTenant(UUID userId, UUID targetTenantId, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Tenant targetTenant = tenantService.getTenantById(targetTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + targetTenantId));

        if (!"ACTIVE".equalsIgnoreCase(targetTenant.getStatus())) {
            throw new AccessDeniedException("Target tenant is not active: " + targetTenant.getStatus());
        }

        TenantMembership membership = membershipRepository.findByTenantAndUser(targetTenant, user)
                .orElseThrow(() -> new AccessDeniedException("User is not a member of target tenant"));

        if (!"ACTIVE".equalsIgnoreCase(membership.getStatus())) {
            throw new AccessDeniedException("Membership in target tenant is " + membership.getStatus());
        }

        List<String> roleCodes = membership.getRoles().stream().map(Role::getCode).toList();
        List<String> permissions = membership.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getCode).distinct().toList();

        SessionService.RotatedTokenPair tokenPair = sessionService.createSession(
                user,
                targetTenant,
                ipAddress,
                userAgent,
                "WEB"
        );

        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                targetTenant.getId(),
                targetTenant.getSlug(),
                tokenPair.session().getId(),
                roleCodes,
                permissions
        );

        auditService.recordEvent("TENANT_SWITCHED", user.getId(), user.getEmail(), targetTenant.getId(), ipAddress, userAgent, "SUCCESS", null);

        UserSummaryDto userSummary = new UserSummaryDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getAvatarUrl());
        TenantSummaryDto tenantSummary = new TenantSummaryDto(targetTenant.getId(), targetTenant.getSlug(), targetTenant.getName(), targetTenant.getStatus());

        return new LoginResponse(
                accessToken,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                userSummary,
                tenantSummary,
                roleCodes,
                permissions,
                tokenPair.newRawRefreshToken()
        );
    }

    /**
     * Operator-controlled method to bootstrap tenant and privileged administrator.
     */
    public Map<String, Object> bootstrapAdmin(BootstrapAdminRequest request, String ipAddress, String userAgent) {
        boolean isProduction = "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
        if (isProduction && ("dev-bootstrap-secret-2026".equals(configuredBootstrapSecret) || !org.springframework.util.StringUtils.hasText(configuredBootstrapSecret))) {
            throw new IllegalStateException("Administrator bootstrap endpoint is disabled in production with default secrets. A dedicated external secret must be configured via NEXARATS_AUTH_BOOTSTRAP_SECRET.");
        }

        if (request.bootstrapSecret() == null || !MessageDigest.isEqual(
                configuredBootstrapSecret.getBytes(StandardCharsets.UTF_8),
                request.bootstrapSecret().getBytes(StandardCharsets.UTF_8))) {
            auditService.recordEvent("BOOTSTRAP_ADMIN_FAILURE", null, request.email(), null, ipAddress, userAgent, "BLOCKED", "Invalid bootstrap secret");
            throw new AccessDeniedException("Invalid administrator bootstrap secret");
        }

        Tenant tenant = tenantService.getOrCreateTenant(request.tenantSlug().trim().toLowerCase(), request.tenantName().trim());

        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User newUser = new User(email, request.firstName().trim(), request.lastName().trim());
            newUser.setEmailVerified(true);
            return userRepository.save(newUser);
        });

        if (credentialRepository.findByUser(user).isEmpty()) {
            passwordService.validatePasswordPolicy(request.password());
            UserCredential cred = new UserCredential(user, passwordService.encode(request.password()));
            credentialRepository.save(cred);
        }

        TenantMembership membership = membershipRepository.findByTenantAndUser(tenant, user).orElseGet(() -> {
            TenantMembership newMembership = new TenantMembership(tenant, user, "ACTIVE");
            return newMembership;
        });

        Role adminRole = roleRepository.findByCode("ROLE_ACADEMY_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ACADEMY_ADMIN missing"));
        Role superAdminRole = roleRepository.findByCode("ROLE_SUPER_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN missing"));

        membership.addRole(adminRole);
        membership.addRole(superAdminRole);
        membershipRepository.save(membership);

        auditService.recordEvent("ADMIN_BOOTSTRAPPED", user.getId(), email, tenant.getId(), ipAddress, userAgent, "SUCCESS", "Administrator provisioned");

        return Map.of(
                "status", "SUCCESS",
                "tenantId", tenant.getId(),
                "tenantSlug", tenant.getSlug(),
                "userId", user.getId(),
                "email", user.getEmail(),
                "roles", List.of("ROLE_ACADEMY_ADMIN", "ROLE_SUPER_ADMIN")
        );
    }

    private Tenant resolveTenantForUser(User user, String requestedSlug) {
        if (requestedSlug != null && !requestedSlug.isBlank()) {
            Tenant tenant = tenantService.getTenantBySlug(requestedSlug.trim())
                    .orElseThrow(() -> new BadCredentialsException("Tenant not found: " + requestedSlug));
            if (!"ACTIVE".equalsIgnoreCase(tenant.getStatus())) {
                throw new BadCredentialsException("Tenant is inactive or suspended: " + requestedSlug);
            }
            return tenant;
        }

        List<TenantMembership> memberships = membershipRepository.findByUserId(user.getId());
        if (memberships.isEmpty()) {
            // Default tenant assignment for new users
            return tenantService.getOrCreateTenant("nexarats-main", "Nexarats Academy");
        }

        // Pick first active membership
        for (TenantMembership tm : memberships) {
            if ("ACTIVE".equalsIgnoreCase(tm.getStatus()) && "ACTIVE".equalsIgnoreCase(tm.getTenant().getStatus())) {
                return tm.getTenant();
            }
        }

        throw new BadCredentialsException("User has no active organization memberships");
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
