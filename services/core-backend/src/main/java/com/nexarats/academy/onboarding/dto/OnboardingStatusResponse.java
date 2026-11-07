package com.nexarats.academy.onboarding.dto;

import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.onboarding.model.OnboardingStatus;

import java.util.List;
import java.util.UUID;

public record OnboardingStatusResponse(
        OnboardingStatus overallStatus,
        boolean profileCompleted,
        boolean developerLinksCompleted,
        boolean avatarUploaded,
        boolean legalAgreementsAccepted,
        List<String> pendingAgreements,
        boolean canActivate,
        StudentProfileDto studentProfile,
        UUID activeEnrollmentId,
        EnrollmentStatus activeEnrollmentStatus,
        List<String> blockingReasons
) {
    public OnboardingStatusResponse(
            OnboardingStatus overallStatus,
            boolean profileCompleted,
            boolean developerLinksCompleted,
            boolean avatarUploaded,
            boolean legalAgreementsAccepted,
            List<String> pendingAgreements,
            boolean canActivate,
            StudentProfileDto studentProfile,
            UUID activeEnrollmentId,
            EnrollmentStatus activeEnrollmentStatus
    ) {
        this(overallStatus, profileCompleted, developerLinksCompleted, avatarUploaded,
                legalAgreementsAccepted, pendingAgreements, canActivate, studentProfile,
                activeEnrollmentId, activeEnrollmentStatus, List.of());
    }
}
