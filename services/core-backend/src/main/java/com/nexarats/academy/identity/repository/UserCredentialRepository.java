package com.nexarats.academy.identity.repository;

import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {
    Optional<UserCredential> findByUser(User user);
    Optional<UserCredential> findByUserId(UUID userId);
}
