package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.PasswordResetToken;
import com.nexarats.academy.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.isUsed = true WHERE p.user = :user AND p.isUsed = false")
    int invalidateAllForUser(User user);
}
