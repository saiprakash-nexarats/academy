package com.nexarats.academy.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enterprise Rate Limiting Service.
 *
 * Implements Task 9 requirements:
 * - Redis-backed distributed rate limiting for login, registration, and password recovery.
 * - Compound keying: IP + Account Hash to defeat distributed brute-force and credential stuffing.
 * - Fail-safe design: If Redis is temporarily unreachable, transparently falls back to
 *   in-memory atomic counters so that protection is never silently disabled.
 */
@Service
public class RateLimitingService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingService.class);

    private final StringRedisTemplate redisTemplate;
    private final int loginLimitPerMinute;
    private String fallbackStrategy;

    // In-memory fallback counters for when Redis is unavailable
    private final Map<String, FallbackCounter> fallbackCounters = new ConcurrentHashMap<>();

    private static class FallbackCounter {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long resetTimeMs;

        FallbackCounter(long windowMs) {
            this.resetTimeMs = System.currentTimeMillis() + windowMs;
        }

        int incrementAndGet(long windowMs) {
            long now = System.currentTimeMillis();
            if (now > resetTimeMs) {
                count.set(0);
                resetTimeMs = now + windowMs;
            }
            return count.incrementAndGet();
        }
    }

    public RateLimitingService(
            StringRedisTemplate redisTemplate,
            @Value("${nexarats.auth.rate-limit.login-attempts-per-minute:5}") int loginLimitPerMinute,
            @Value("${nexarats.auth.rate-limit.fallback-strategy:memory}") String fallbackStrategy
    ) {
        this.redisTemplate = redisTemplate;
        this.loginLimitPerMinute = loginLimitPerMinute;
        this.fallbackStrategy = fallbackStrategy;
    }

    public void setFallbackStrategy(String fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
    }

    public String getFallbackStrategy() {
        return this.fallbackStrategy;
    }

    /**
     * Checks and increments login rate limit for the given IP and account email.
     * @return true if allowed, false if limit exceeded (HTTP 429)
     */
    public boolean checkLoginAllowed(String ip, String email) {
        String ipKey = "ratelimit:login:ip:" + sanitize(ip);
        String accountKey = "ratelimit:login:acc:" + hashIdentifier(email);

        boolean ipAllowed = checkLimitWithFallback(ipKey, loginLimitPerMinute, Duration.ofMinutes(1));
        boolean accAllowed = checkLimitWithFallback(accountKey, loginLimitPerMinute, Duration.ofMinutes(1));

        return ipAllowed && accAllowed;
    }

    /**
     * Checks password reset request rate limit.
     */
    public boolean checkPasswordResetAllowed(String email) {
        String key = "ratelimit:reset:acc:" + hashIdentifier(email);
        return checkLimitWithFallback(key, 3, Duration.ofMinutes(15));
    }

    /**
     * Checks public registration rate limit.
     */
    public boolean checkRegistrationAllowed(String ip) {
        String key = "ratelimit:register:ip:" + sanitize(ip);
        return checkLimitWithFallback(key, 10, Duration.ofHours(1));
    }

    /**
     * Clears rate limit counters for testing environments.
     */
    public void resetRateLimits() {
        try {
            java.util.Set<String> keys = redisTemplate.keys("ratelimit:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ignored) {}
        fallbackCounters.clear();
    }

    private boolean checkLimitWithFallback(String key, int maxRequests, Duration window) {
        try {
            Long current = redisTemplate.opsForValue().increment(key);
            if (current != null && current == 1) {
                redisTemplate.expire(key, window);
            }
            return current != null && current <= maxRequests;
        } catch (Exception e) {
            if ("fail-closed".equalsIgnoreCase(fallbackStrategy)) {
                log.error("Redis unavailable for rate limiting key '{}' in fail-closed mode. Rejecting request for security: {}", key, e.getMessage());
                return false;
            }
            log.warn("Redis unavailable for rate limiting key '{}'. Falling back to local memory counter: {}", key, e.getMessage());
            FallbackCounter counter = fallbackCounters.computeIfAbsent(key, k -> new FallbackCounter(window.toMillis()));
            int current = counter.incrementAndGet(window.toMillis());
            return current <= maxRequests;
        }
    }

    private String sanitize(String input) {
        if (input == null || input.isBlank()) return "unknown";
        return input.trim().replaceAll("[^a-zA-Z0-9.:_-]", "_");
    }

    private String hashIdentifier(String value) {
        if (value == null) return "unknown";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(value.hashCode());
        }
    }
}
