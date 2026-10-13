package com.nexarats.academy.admissions.dto;

import com.nexarats.academy.admissions.model.ApplicationStatus;
import java.time.Instant;
import java.util.UUID;

public record ApplicationSummaryDto(
        UUID id,
        String applicationNumber,
        String fullName,
        String email,
        String phone,
        UUID programId,
        UUID courseId,
        UUID cohortId,
        String experienceLevel,
        ApplicationStatus status,
        Instant submittedAt
) {}
