package com.nexarats.academy.onboarding.repository;

import com.nexarats.academy.onboarding.model.AvatarAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvatarAssetRepository extends JpaRepository<AvatarAsset, UUID> {

    Optional<AvatarAsset> findByIdAndUserId(UUID id, UUID userId);

    List<AvatarAsset> findByUserIdOrderByUploadedAtDesc(UUID userId);

    Optional<AvatarAsset> findFirstByUserIdAndIsActiveTrueOrderByUploadedAtDesc(UUID userId);
}
