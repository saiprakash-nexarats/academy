package com.nexarats.academy.onboarding.dto;

import com.nexarats.academy.onboarding.model.LegalAgreement;
import com.nexarats.academy.onboarding.model.LegalAgreementType;

import java.time.Instant;
import java.util.UUID;

public record LegalAgreementDto(
        UUID id,
        LegalAgreementType agreementType,
        String version,
        String title,
        String content,
        String contentHash,
        Instant effectiveDate,
        boolean isAccepted,
        Instant acceptedAt
) {
    public static LegalAgreementDto fromEntity(LegalAgreement agreement, boolean isAccepted, Instant acceptedAt) {
        return new LegalAgreementDto(
                agreement.getId(),
                agreement.getAgreementType(),
                agreement.getVersion(),
                agreement.getTitle(),
                agreement.getContent(),
                agreement.getContentHash(),
                agreement.getEffectiveDate(),
                isAccepted,
                acceptedAt
        );
    }
}
