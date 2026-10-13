package com.nexarats.academy.admissions.dto;

import com.nexarats.academy.admissions.model.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public record EnrollmentDto(
        UUID id,
        String enrollmentNumber,
        UUID learnerUserId,
        UUID programId,
        UUID courseId,
        UUID cohortId,
        UUID applicationId,
        EnrollmentStatus status,
        Instant enrolledAt
) {}
