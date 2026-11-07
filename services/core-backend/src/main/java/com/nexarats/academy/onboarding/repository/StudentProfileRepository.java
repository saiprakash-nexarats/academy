package com.nexarats.academy.onboarding.repository;

import com.nexarats.academy.onboarding.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    Optional<StudentProfile> findByTenantIdAndUserId(UUID tenantId, UUID userId);

    Optional<StudentProfile> findByUserId(UUID userId);
}
