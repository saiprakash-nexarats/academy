package com.nexarats.academy.onboarding.service;

import com.nexarats.academy.onboarding.dto.StudentProfileDto;
import com.nexarats.academy.onboarding.dto.UpdateProfileRequest;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.AvatarAssetRepository;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StudentProfileService {

    private static final Logger log = LoggerFactory.getLogger(StudentProfileService.class);

    private final StudentProfileRepository profileRepository;
    private final AvatarAssetRepository avatarAssetRepository;

    public StudentProfileService(StudentProfileRepository profileRepository,
                                 AvatarAssetRepository avatarAssetRepository) {
        this.profileRepository = profileRepository;
        this.avatarAssetRepository = avatarAssetRepository;
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
        StudentProfile profile = getOrCreateProfile(tenantId, userId);
        profile.setAvatarAssetId(avatarAssetId);
        if (profile.getOnboardingStatus() == OnboardingStatus.NOT_STARTED) {
            profile.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        }
        profile = profileRepository.save(profile);
        log.info("Linked avatar asset {} to student profile for user {}", avatarAssetId, userId);
        return profile;
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
