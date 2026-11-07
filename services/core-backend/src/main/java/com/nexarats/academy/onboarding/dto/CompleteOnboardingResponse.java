package com.nexarats.academy.onboarding.dto;

import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.onboarding.model.OnboardingStatus;

import java.time.Instant;

public record CompleteOnboardingResponse(
        boolean success,
        String message,
        OnboardingStatus onboardingStatus,
        Instant completedAt,
        EnrollmentStatus enrollmentStatus
) {
}
