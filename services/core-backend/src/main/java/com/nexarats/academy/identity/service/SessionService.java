package com.nexarats.academy.identity.service;

import com.nexarats.academy.common.security.SessionValidator;
import com.nexarats.academy.identity.model.RefreshToken;
import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.model.UserSession;
import com.nexarats.academy.identity.repository.RefreshTokenRepository;
import com.nexarats.academy.identity.repository.UserSessionRepository;
import com.nexarats.academy.tenant.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Enterprise Session & Refresh Token Service.
 *
 * Implements Task 4 & Task 9:
 * - Opaque cryptographically random refresh tokens.
 * - Server-side SHA-256 token hash persistence.
 * - Single-use refresh token rotation with immediate invalidation.
 * - Token replay/reuse detection triggering full session revocation.
 * - Redis active-session synchronization with PostgreSQL fallback.
 */
@Service
@Transactional
public class SessionService implements SessionValidator {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final UserSessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;
    private final AuditService auditService;
    private final long refreshTokenExpirationSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public record RotatedTokenPair(
            UserSession session,
            String newRawRefreshToken,
            Instant expiresAt
    ) {}

    public SessionService(
            UserSessionRepository sessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            StringRedisTemplate redisTemplate,
            AuditService auditService,
            @Value("${nexarats.auth.jwt.refresh-token-expiration-seconds:604800}") long refreshTokenExpirationSeconds
    ) {
        this.sessionRepository = sessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisTemplate = redisTemplate;
        this.auditService = auditService;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    /**
     * Checks if a session is currently active (not revoked and not expired).
     * Implements SessionValidator interface for JwtAuthenticationFilter.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isSessionActive(UUID sessionId) {
        if (sessionId == null) return false;

        String redisKey = "session:active:" + sessionId;
        try {
            String cached = redisTemplate.opsForValue().get(redisKey);
            if ("revoked".equals(cached)) {
                return false;
            }
            if ("valid".equals(cached)) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Redis check failed for session {}: {}. Falling back to DB.", sessionId, e.getMessage());
        }

        Optional<UserSession> sessionOpt = sessionRepository.findById(sessionId);
        boolean active = sessionOpt.map(UserSession::isActive).orElse(false);

        try {
            redisTemplate.opsForValue().set(redisKey, active ? "valid" : "revoked", Duration.ofMinutes(15));
        } catch (Exception ignored) {}

        return active;
    }

    /**
     * Creates a new UserSession and issues the first opaque RefreshToken.
     */
    public RotatedTokenPair createSession(
            User user,
            Tenant tenant,
            String ipAddress,
            String userAgent,
            String clientType
    ) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(refreshTokenExpirationSeconds);

        UserSession session = new UserSession(user, tenant, ipAddress, userAgent, clientType, expiresAt);
        session = sessionRepository.save(session);

        String rawToken = generateOpaqueToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken(session, user, tenant, tokenHash, 1, expiresAt);
        refreshTokenRepository.save(refreshToken);

        cacheSessionInRedis(session.getId(), expiresAt);
        cacheRefreshTokenInRedis(tokenHash, session.getId(), user.getId(), expiresAt);

        return new RotatedTokenPair(session, rawToken, expiresAt);
    }

    /**
     * Atomically rotates a refresh token.
     * Enforces single-use semantics and detects replay attacks.
     * If replay is detected, session revocation and security audit must commit.
     */
    @Transactional(noRollbackFor = IllegalStateException.class)
    public RotatedTokenPair rotateRefreshToken(String rawRefreshToken, String ipAddress, String userAgent) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        String presentedHash = hashToken(rawRefreshToken.trim());
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(presentedHash);

        if (tokenOpt.isEmpty()) {
            log.warn("Attempt to refresh with unknown token hash: {}", presentedHash);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        RefreshToken oldToken = tokenOpt.get();

        // REPLAY / REUSE DETECTION
        if (oldToken.isRevoked() || oldToken.getReplacedByTokenHash() != null) {
            log.error("CRITICAL: Refresh token reuse detected for user {}! Token hash: {}",
                    oldToken.getUser().getId(), presentedHash);

            // Immediate defense: Revoke ALL sessions and tokens for this user
            revokeAllSessionsForUser(oldToken.getUser().getId());

            auditService.recordEvent(
                    "TOKEN_REUSE_DETECTED",
                    oldToken.getUser().getId(),
                    oldToken.getUser().getEmail(),
                    oldToken.getTenant().getId(),
                    ipAddress,
                    userAgent,
                    "BLOCKED",
                    "Superseded refresh token replay attempted; all sessions revoked"
            );

            throw new IllegalStateException("Security breach detected: refresh token reuse. All sessions have been revoked.");
        }

        if (oldToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        UserSession session = oldToken.getSession();
        if (!isSessionActive(session.getId()) || !session.isActive()) {
            throw new IllegalArgumentException("Session has been terminated or expired");
        }

        // Issue new rotated token
        String newRawToken = generateOpaqueToken();
        String newHash = hashToken(newRawToken);
        Instant newExpiresAt = Instant.now().plusSeconds(refreshTokenExpirationSeconds);

        oldToken.revoke(newHash);
        refreshTokenRepository.save(oldToken);

        RefreshToken newToken = new RefreshToken(
                session,
                oldToken.getUser(),
                oldToken.getTenant(),
                newHash,
                oldToken.getSequenceNumber() + 1,
                newExpiresAt
        );
        refreshTokenRepository.save(newToken);

        session.setLastActiveAt(Instant.now());
        sessionRepository.save(session);

        // Invalidate old token in Redis, cache new token
        removeRefreshTokenFromRedis(presentedHash);
        cacheRefreshTokenInRedis(newHash, session.getId(), oldToken.getUser().getId(), newExpiresAt);

        return new RotatedTokenPair(session, newRawToken, newExpiresAt);
    }

    /**
     * Revokes an active session and all its associated tokens.
     */
    public void revokeSession(UUID sessionId) {
        if (sessionId == null) return;

        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.revoke();
            sessionRepository.save(session);
            refreshTokenRepository.revokeAllBySessionId(sessionId, Instant.now());
            invalidateSessionInRedis(sessionId);
        });
    }

    /**
     * Revokes session associated with given raw refresh token.
     */
    public void revokeSessionByRefreshToken(String rawRefreshToken) {
        if (!org.springframework.util.StringUtils.hasText(rawRefreshToken)) return;
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            revokeSession(token.getSession().getId());
        });
    }

    /**
     * Revokes all active sessions for a user (e.g. after password change or compromise).
     */
    public void revokeAllSessionsForUser(UUID userId) {
        if (userId == null) return;

        // Invalidate all active cached sessions in Redis before marking them revoked in DB
        try {
            sessionRepository.findByUserIdAndIsRevokedFalse(userId).forEach(s -> {
                invalidateSessionInRedis(s.getId());
            });
        } catch (Exception ignored) {}

        sessionRepository.revokeAllByUserId(userId, Instant.now());
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    private void cacheSessionInRedis(UUID sessionId, Instant expiresAt) {
        try {
            long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set("session:active:" + sessionId, "valid", Duration.ofSeconds(ttlSeconds));
            }
        } catch (Exception e) {
            log.warn("Redis write failed for session {}: {}", sessionId, e.getMessage());
        }
    }

    private void invalidateSessionInRedis(UUID sessionId) {
        try {
            redisTemplate.opsForValue().set("session:active:" + sessionId, "revoked", Duration.ofHours(24));
        } catch (Exception ignored) {}
    }

    private void cacheRefreshTokenInRedis(String tokenHash, UUID sessionId, UUID userId, Instant expiresAt) {
        try {
            long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set("auth:refresh:" + tokenHash, sessionId + ":" + userId, Duration.ofSeconds(ttlSeconds));
            }
        } catch (Exception ignored) {}
    }

    private void removeRefreshTokenFromRedis(String tokenHash) {
        try {
            redisTemplate.delete("auth:refresh:" + tokenHash);
        } catch (Exception ignored) {}
    }
}
