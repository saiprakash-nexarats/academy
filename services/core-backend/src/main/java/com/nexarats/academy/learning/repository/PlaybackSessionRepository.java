package com.nexarats.academy.learning.repository;

import com.nexarats.academy.learning.model.PlaybackSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaybackSessionRepository extends JpaRepository<PlaybackSession, UUID> {

    Optional<PlaybackSession> findBySessionToken(String sessionToken);

    void deleteByExpiresAtBefore(Instant timestamp);
}
