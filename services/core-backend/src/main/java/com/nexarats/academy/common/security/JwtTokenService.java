package com.nexarats.academy.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

/**
 * Enterprise Asymmetric JWT Service (RS256).
 *
 * Implements Task 4 requirements:
 * - Asymmetric signatures using RSA-2048 with SHA-256 (RS256).
 * - Enforces algorithm, issuer, audience, and temporal validity.
 * - Key management: Ephemeral in-memory keypair for dev/test; requires stable, externally
 *   configured RSA keys in production environments.
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final String issuer;
    private final String audience;
    private final long accessTokenExpirationSeconds;
    private final String keyId;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtTokenService(
            @Value("${nexarats.auth.jwt.issuer:nexarats-academy}") String issuer,
            @Value("${nexarats.auth.jwt.audience:nexarats-clients}") String audience,
            @Value("${nexarats.auth.jwt.access-token-expiration-seconds:900}") long accessTokenExpirationSeconds,
            @Value("${nexarats.auth.jwt.private-key-pem:}") String privateKeyPem,
            @Value("${nexarats.auth.jwt.public-key-pem:}") String publicKeyPem,
            @Value("${spring.profiles.active:local}") String activeProfile
    ) {
        this.issuer = issuer;
        this.audience = audience;
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;

        if (StringUtils.hasText(privateKeyPem) && StringUtils.hasText(publicKeyPem)) {
            this.privateKey = parsePrivateKey(privateKeyPem);
            this.publicKey = parsePublicKey(publicKeyPem);
            this.keyId = "nexarats-configured-rsa-key";
            log.info("Initialized JwtTokenService with externally configured stable RSA keys.");
        } else {
            boolean isProduction = "prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile);
            if (isProduction) {
                throw new IllegalStateException("Production profile active, but no external RSA keys configured! Ephemeral runtime key generation is strictly forbidden in production. Configure 'nexarats.auth.jwt.private-key-pem' and 'public-key-pem'.");
            }
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);
                KeyPair kp = kpg.generateKeyPair();
                this.privateKey = (RSAPrivateKey) kp.getPrivate();
                this.publicKey = (RSAPublicKey) kp.getPublic();
                this.keyId = "nexarats-rsa-key-" + UUID.randomUUID().toString().substring(0, 8);
                log.info("Initialized secure RSA-2048 keypair with keyId='{}' for RS256 token signing (profile: {})", this.keyId, activeProfile);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("RSA algorithm not available in runtime", e);
            }
        }
    }

    private RSAPrivateKey parsePrivateKey(String pem) {
        try {
            String clean = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(clean);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse RSA private key PEM: " + e.getMessage(), e);
        }
    }

    private RSAPublicKey parsePublicKey(String pem) {
        try {
            String clean = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                    .replace("-----END RSA PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(clean);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse RSA public key PEM: " + e.getMessage(), e);
        }
    }

    /**
     * Issues a cryptographically signed RS256 access token.
     */
    public String generateAccessToken(
            UUID userId,
            String email,
            String name,
            UUID tenantId,
            String tenantSlug,
            UUID sessionId,
            Collection<String> roles,
            Collection<String> permissions
    ) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
                .header()
                .keyId(keyId)
                .type("JWT")
                .and()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(userId.toString())
                .claim("email", email)
                .claim("name", name != null ? name : "")
                .claim("tenant_id", tenantId.toString())
                .claim("tenant_slug", tenantSlug != null ? tenantSlug : "")
                .claim("sid", sessionId.toString())
                .claim("roles", roles != null ? new ArrayList<>(roles) : List.of())
                .claim("permissions", permissions != null ? new ArrayList<>(permissions) : List.of())
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Cryptographically parses and validates an RS256 JWT access token.
     * Rejects invalid signatures, expired tokens, or forged claims.
     */
    public Claims validateAndParseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or expired access token: " + e.getMessage(), e);
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String getKeyId() {
        return keyId;
    }
}
