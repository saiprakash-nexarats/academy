package com.nexarats.academy.common.security;

import com.nexarats.academy.common.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Filter that intercepts incoming HTTP requests, validates RS256 Bearer JWTs,
 * validates active session state, and establishes Spring SecurityContext and TenantContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private SessionValidator sessionValidator;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Autowired(required = false)
    public void setSessionValidator(SessionValidator sessionValidator) {
        this.sessionValidator = sessionValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenService.validateAndParseToken(token);

                UUID userId = UUID.fromString(claims.getSubject());
                String email = claims.get("email", String.class);
                String name = claims.get("name", String.class);
                String tenantIdStr = claims.get("tenant_id", String.class);
                String tenantSlug = claims.get("tenant_slug", String.class);
                String sessionIdStr = claims.get("sid", String.class);

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles", List.class);

                @SuppressWarnings("unchecked")
                List<String> permissions = (List<String>) claims.get("permissions", List.class);

                UUID tenantId = tenantIdStr != null ? UUID.fromString(tenantIdStr) : null;
                UUID sessionId = sessionIdStr != null ? UUID.fromString(sessionIdStr) : null;

                // Validate session revocation if session validator is available
                if (sessionId != null && sessionValidator != null && !sessionValidator.isSessionActive(sessionId)) {
                    log.debug("Session {} is revoked or expired. Rejecting request.", sessionId);
                    SecurityContextHolder.clearContext();
                    TenantContext.clear();
                    filterChain.doFilter(request, response);
                    return;
                }

                // Check for client tenant header spoofing attempt:
                // ADR-004: Never blindly trust client tenant header.
                // If client header is present, it must match the authorized token's tenant claim!
                String clientTenantHeader = request.getHeader("X-Tenant-ID");
                if (StringUtils.hasText(clientTenantHeader) && tenantId != null) {
                    try {
                        UUID requestedTenantId = UUID.fromString(clientTenantHeader.trim());
                        if (!requestedTenantId.equals(tenantId)) {
                            log.warn("Forged or mismatched tenant header detected. Token tenant: {}, Header tenant: {}. Rejecting.", tenantId, requestedTenantId);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant header does not match authenticated token scope");
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed X-Tenant-ID header");
                        return;
                    }
                }

                // Establish UserPrincipal and Security Context
                UserPrincipal principal = new UserPrincipal(
                        userId,
                        email,
                        name,
                        tenantId,
                        tenantSlug,
                        sessionId,
                        roles,
                        permissions
                );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Scope TenantContext for this request
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                }

            } catch (Exception e) {
                log.debug("Token authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                TenantContext.clear();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Prevent thread-local leakage on servlet thread pooling
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}
