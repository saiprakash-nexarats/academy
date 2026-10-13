package com.nexarats.academy.cohort.dto;

import com.nexarats.academy.cohort.model.CohortStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CohortDto(
        UUID id,
        String cohortCode,
        String name,
        UUID programId,
        UUID courseId,
        LocalDate startDate,
        LocalDate endDate,
        Instant enrollmentEndDate,
        int capacity,
        int availableSeats,
        CohortStatus status
) {}
