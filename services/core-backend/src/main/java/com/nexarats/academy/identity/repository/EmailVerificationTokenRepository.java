package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.EmailVerificationToken;
import com.nexarats.academy.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE EmailVerificationToken e SET e.isUsed = true WHERE e.user = :user AND e.isUsed = false")
    int invalidateAllForUser(User user);
}
