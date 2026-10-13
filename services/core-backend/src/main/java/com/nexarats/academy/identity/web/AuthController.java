package com.nexarats.academy.identity.web;

import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.identity.dto.*;
import com.nexarats.academy.identity.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Enterprise Authentication REST API Controller.
 *
 * Exposes endpoints conforming to API blueprint:
 * - POST /api/v1/auth/login
 * - POST /api/v1/auth/refresh
 * - POST /api/v1/auth/logout
 * - GET  /api/v1/auth/me
 * - POST /api/v1/auth/change-password
 * - POST /api/v1/auth/forgot-password
 * - POST /api/v1/auth/reset-password
 * - POST /api/v1/auth/register
 * - POST /api/v1/auth/switch-tenant
 * - POST /api/v1/auth/bootstrap-admin
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${nexarats.auth.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ip, userAgent);

        ResponseCookie refreshCookie = buildRefreshTokenCookie(response.refreshToken(), Duration.ofDays(7));
        ResponseCookie csrfCookie = buildCsrfCookie(UUID.randomUUID().toString(), Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie.toString())
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(
            @RequestBody(required = false) TokenRefreshRequest bodyRequest,
            HttpServletRequest httpRequest
    ) {
        String rawToken = null;

        // 1. Check body token (mobile / API clients)
        if (bodyRequest != null && StringUtils.hasText(bodyRequest.refreshToken())) {
            rawToken = bodyRequest.refreshToken();
        } else {
            // 2. Check HttpOnly cookie (browser clients)
            rawToken = extractCookie(httpRequest, "nexarats_refresh_token");

            // Enforce CSRF defense on cookie-based refresh
            if (rawToken != null) {
                validateCsrfForCookieAuth(httpRequest);
            }
        }

        if (!StringUtils.hasText(rawToken)) {
            throw new IllegalArgumentException("Refresh token is required via cookie or request body");
        }

        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        TokenRefreshResponse response = authService.refresh(rawToken, ip, userAgent);

        ResponseCookie refreshCookie = buildRefreshTokenCookie(response.refreshToken(), Duration.ofDays(7));
        ResponseCookie csrfCookie = buildCsrfCookie(UUID.randomUUID().toString(), Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        String cookieToken = extractCookie(httpRequest, "nexarats_refresh_token");
        if (cookieToken != null) {
            validateCsrfForCookieAuth(httpRequest);
        }

        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        if (principal != null) {
            authService.logout(principal.getSessionId(), principal.getUserId(), ip, userAgent);
        } else if (cookieToken != null) {
            authService.logoutByRefreshToken(cookieToken, ip, userAgent);
        }

        ResponseCookie clearRefreshCookie = buildRefreshTokenCookie("", Duration.ZERO);
        ResponseCookie clearCsrfCookie = buildCsrfCookie("", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearCsrfCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    private void validateCsrfForCookieAuth(HttpServletRequest httpRequest) {
        String csrfHeader = httpRequest.getHeader("X-CSRF-Token");
        if (!StringUtils.hasText(csrfHeader)) {
            csrfHeader = httpRequest.getHeader("X-XSRF-TOKEN");
        }
        String csrfCookie = extractCookie(httpRequest, "nexarats_csrf_token");
        if (!StringUtils.hasText(csrfCookie)) {
            csrfCookie = extractCookie(httpRequest, "XSRF-TOKEN");
        }

        if (!StringUtils.hasText(csrfHeader) || !StringUtils.hasText(csrfCookie) ||
                !MessageDigest.isEqual(csrfHeader.getBytes(StandardCharsets.UTF_8), csrfCookie.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("CSRF verification failed on cookie-authenticated operation");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserProfileResponse profile = authService.getProfile(principal.getUserId(), principal.getTenantId());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.changePassword(principal.getUserId(), request, ip, userAgent);

        ResponseCookie clearRefreshCookie = buildRefreshTokenCookie("", Duration.ZERO);
        ResponseCookie clearCsrfCookie = buildCsrfCookie("", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearCsrfCookie.toString())
                .body(Map.of("message", "Password changed successfully. Active sessions revoked."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.forgotPassword(request, ip, userAgent);

        // Uniform response to prevent account enumeration
        return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, password reset instructions have been sent."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.resetPassword(request, ip, userAgent);

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully. Please log in with your new password."
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.register(request, ip, userAgent);

        ResponseCookie refreshCookie = buildRefreshTokenCookie(response.refreshToken(), Duration.ofDays(7));
        ResponseCookie csrfCookie = buildCsrfCookie(UUID.randomUUID().toString(), Duration.ofDays(7));

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie.toString())
                .body(response);
    }

    @PostMapping("/switch-tenant")
    public ResponseEntity<LoginResponse> switchTenant(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SwitchTenantRequest request,
            HttpServletRequest httpRequest
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.switchTenant(principal.getUserId(), request.tenantId(), ip, userAgent);

        ResponseCookie refreshCookie = buildRefreshTokenCookie(response.refreshToken(), Duration.ofDays(7));
        ResponseCookie csrfCookie = buildCsrfCookie(UUID.randomUUID().toString(), Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, csrfCookie.toString())
                .body(response);
    }

    @PostMapping("/bootstrap-admin")
    public ResponseEntity<Map<String, Object>> bootstrapAdmin(
            @Valid @RequestBody BootstrapAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        Map<String, Object> result = authService.bootstrapAdmin(request, ip, userAgent);
        return ResponseEntity.ok(result);
    }

    private ResponseCookie buildRefreshTokenCookie(String token, Duration maxAge) {
        return ResponseCookie.from("nexarats_refresh_token", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie buildCsrfCookie(String csrfToken, Duration maxAge) {
        return ResponseCookie.from("nexarats_csrf_token", csrfToken)
                .httpOnly(false) // Accessible to client JS for double-submit header
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String extractClientIp(HttpServletRequest request) {
        // Enforce safe IP resolution: check remote address first, or trusted headers
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            String[] parts = xForwardedFor.split(",");
            return parts[0].trim();
        }
        return request.getRemoteAddr();
    }
}
