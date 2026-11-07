package com.nexarats.academy.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptAgreementRequest(
        @NotBlank(message = "Agreement version is required")
        @Size(max = 32, message = "Version must not exceed 32 characters")
        String agreementVersion
) {
}
