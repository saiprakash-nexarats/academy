package com.nexarats.academy.admissions.dto;

import com.nexarats.academy.admissions.model.AdmissionOfferStatus;
import java.time.Instant;
import java.util.UUID;

public record AdmissionOfferDto(
        UUID id,
        String offerNumber,
        UUID applicationId,
        UUID cohortId,
        long offeredAmountCents,
        String currency,
        AdmissionOfferStatus status,
        Instant validUntil,
        Instant issuedAt
) {}
