package com.nexarats.academy.onboarding.dto;

import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;

import java.time.Instant;
import java.util.UUID;

public record StudentProfileDto(
        UUID id,
        UUID tenantId,
        UUID userId,
        String headline,
        String bio,
        String academicBackground,
        String careerGoals,
        String timezone,
        String githubUrl,
        String linkedinUrl,
        UUID avatarAssetId,
        String avatarUrl,
        OnboardingStatus onboardingStatus,
        Instant onboardingCompletedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static StudentProfileDto fromEntity(StudentProfile profile, String avatarUrl) {
        if (profile == null) {
            return null;
        }
        return new StudentProfileDto(
                profile.getId(),
                profile.getTenantId(),
                profile.getUserId(),
                profile.getHeadline(),
                profile.getBio(),
                profile.getAcademicBackground(),
                profile.getCareerGoals(),
                profile.getTimezone(),
                profile.getGithubUrl(),
                profile.getLinkedinUrl(),
                profile.getAvatarAssetId(),
                avatarUrl,
                profile.getOnboardingStatus(),
                profile.getOnboardingCompletedAt(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
