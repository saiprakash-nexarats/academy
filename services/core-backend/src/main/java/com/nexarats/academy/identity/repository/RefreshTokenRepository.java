package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("SELECT r FROM RefreshToken r JOIN FETCH r.user JOIN FETCH r.tenant JOIN FETCH r.session WHERE r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE refresh_tokens SET is_revoked = true, revoked_at = :revokedAt WHERE session_id = CAST(:sessionId AS uuid) AND is_revoked = false", nativeQuery = true)
    int revokeAllBySessionId(UUID sessionId, Instant revokedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE refresh_tokens SET is_revoked = true, revoked_at = :revokedAt WHERE user_id = CAST(:userId AS uuid) AND is_revoked = false", nativeQuery = true)
    int revokeAllByUserId(UUID userId, Instant revokedAt);
}
