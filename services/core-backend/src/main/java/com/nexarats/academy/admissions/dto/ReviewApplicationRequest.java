package com.nexarats.academy.admissions.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ReviewApplicationRequest(
        @NotBlank(message = "Decision is required (APPROVED or REJECTED)")
        String decision,

        String reviewNotes,

        String rejectionReason,

        UUID targetCohortId,

        Long customTuitionCents,

        String currency
) {}
