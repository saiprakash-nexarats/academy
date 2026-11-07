package com.nexarats.academy.onboarding.service;

import com.nexarats.academy.identity.model.User;
import com.nexarats.academy.identity.repository.UserRepository;
import com.nexarats.academy.onboarding.dto.StudentProfileDto;
import com.nexarats.academy.onboarding.dto.UpdateProfileRequest;
import com.nexarats.academy.onboarding.model.AvatarAsset;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class StudentProfileService {

    private static final Logger log = LoggerFactory.getLogger(StudentProfileService.class);

    private final StudentProfileRepository profileRepository;
    private final AvatarAssetRepository avatarAssetRepository;
    private final UserRepository userRepository;
    private final AvatarStorageService storageService;

    public StudentProfileService(StudentProfileRepository profileRepository,
                                 AvatarAssetRepository avatarAssetRepository,
                                 UserRepository userRepository,
                                 AvatarStorageService storageService) {
        this.profileRepository = profileRepository;
        this.avatarAssetRepository = avatarAssetRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional
    public StudentProfile getOrCreateProfile(UUID tenantId, UUID userId) {
        return profileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseGet(() -> {
                    log.info("Creating initial StudentProfile for user={} tenant={}", userId, tenantId);
                    StudentProfile newProfile = new StudentProfile(tenantId, userId);
                    return profileRepository.save(newProfile);
                });
    }

    @Transactional
    public StudentProfileDto updateProfile(UUID tenantId, UUID userId, UpdateProfileRequest request) {
        StudentProfile profile = getOrCreateProfile(tenantId, userId);

        if (request.headline() != null) {
            profile.setHeadline(request.headline().trim());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio().trim());
        }
        if (request.academicBackground() != null) {
            profile.setAcademicBackground(request.academicBackground().trim());
        }
        if (request.careerGoals() != null) {
            profile.setCareerGoals(request.careerGoals().trim());
        }
        if (request.timezone() != null && !request.timezone().isBlank()) {
            profile.setTimezone(request.timezone().trim());
        }
        if (request.githubUrl() != null) {
            profile.setGithubUrl(request.githubUrl().trim());
        }
        if (request.linkedinUrl() != null) {
            profile.setLinkedinUrl(request.linkedinUrl().trim());
        }

        // If currently NOT_STARTED, move to IN_PROGRESS upon updating profile details
        if (profile.getOnboardingStatus() == OnboardingStatus.NOT_STARTED) {
            profile.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        }

        profile = profileRepository.save(profile);
        log.info("Updated StudentProfile for user {} tenant {}", userId, tenantId);

        String avatarUrl = resolveAvatarUrl(profile.getAvatarAssetId());
        return StudentProfileDto.fromEntity(profile, avatarUrl);
    }

    @Transactional
    public StudentProfile linkAvatarAsset(UUID tenantId, UUID userId, UUID avatarAssetId) {
        if (avatarAssetId != null) {
            AvatarAsset newAsset = avatarAssetRepository.findById(avatarAssetId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar asset not found: " + avatarAssetId));
            if (!newAsset.getUserId().equals(userId)) {
                log.warn("Cross-user avatar linking rejected: user {} tried to link asset {} belonging to user {}",
                        userId, avatarAssetId, newAsset.getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot link an avatar asset owned by another user.");
            }
            if (newAsset.getTenantId() != null && !newAsset.getTenantId().equals(tenantId)) {
                log.warn("Cross-tenant avatar linking rejected: tenant {} tried to link asset {} from tenant {}",
                        tenantId, avatarAssetId, newAsset.getTenantId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot link an avatar asset from another tenant.");
            }
        }

        StudentProfile profile = getOrCreateProfile(tenantId, userId);
        UUID oldAssetId = profile.getAvatarAssetId();

        // Safe replacement: if previous avatar asset exists and is different, deactivate and clean up
        if (oldAssetId != null && !oldAssetId.equals(avatarAssetId)) {
            avatarAssetRepository.findById(oldAssetId).ifPresent(oldAsset -> {
                oldAsset.setActive(false);
                avatarAssetRepository.save(oldAsset);
                storageService.deleteAvatar(oldAsset.getStorageObjectKey());
                log.info("Cleaned up previous replaced avatar asset {} for user {}", oldAssetId, userId);
            });
        }

        profile.setAvatarAssetId(avatarAssetId);
        if (profile.getOnboardingStatus() == OnboardingStatus.NOT_STARTED) {
            profile.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        }
        profile = profileRepository.save(profile);
        log.info("Linked avatar asset {} to student profile for user {}", avatarAssetId, userId);
        return profile;
    }

    @Transactional
    public void unlinkAvatar(UUID tenantId, UUID userId) {
        StudentProfile profile = profileRepository.findByTenantIdAndUserId(tenantId, userId).orElse(null);
        if (profile != null && profile.getAvatarAssetId() != null) {
            UUID oldAssetId = profile.getAvatarAssetId();
            profile.setAvatarAssetId(null);
            profileRepository.save(profile);

            // Also clear User.avatarUrl
            userRepository.findById(userId).ifPresent(u -> {
                u.setAvatarUrl(null);
                userRepository.save(u);
            });

            // Mark old asset inactive and delete file
            avatarAssetRepository.findById(oldAssetId).ifPresent(asset -> {
                asset.setActive(false);
                avatarAssetRepository.save(asset);
                storageService.deleteAvatar(asset.getStorageObjectKey());
                log.info("Safely unlinked and deleted avatar asset {} for user {}", oldAssetId, userId);
            });
        }
    }

    @Transactional(readOnly = true)
    public StudentProfileDto getProfileDto(UUID tenantId, UUID userId) {
        StudentProfile profile = profileRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElse(null);
        if (profile == null) {
            return null;
        }
        String avatarUrl = resolveAvatarUrl(profile.getAvatarAssetId());
        return StudentProfileDto.fromEntity(profile, avatarUrl);
    }

    public String resolveAvatarUrl(UUID avatarAssetId) {
        if (avatarAssetId == null) {
            return null;
        }
        return "/api/v1/onboarding/avatar/" + avatarAssetId;
    }
}
