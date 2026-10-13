package com.nexarats.academy.admissions.dto;

import com.nexarats.academy.cohort.model.SeatReservationStatus;
import java.time.Instant;
import java.util.UUID;

public record SeatReservationDto(
        UUID id,
        UUID cohortId,
        String reservationToken,
        SeatReservationStatus status,
        Instant expiresAt
) {}
