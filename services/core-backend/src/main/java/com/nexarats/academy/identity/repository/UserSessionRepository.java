package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    List<UserSession> findByUserAndIsRevokedFalse(User user);
    List<UserSession> findByUserIdAndIsRevokedFalse(UUID userId);
    Optional<UserSession> findByIdAndIsRevokedFalse(UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE user_sessions SET is_revoked = true, revoked_at = :revokedAt WHERE user_id = CAST(:userId AS uuid) AND is_revoked = false", nativeQuery = true)
    int revokeAllByUserId(UUID userId, Instant revokedAt);
}
