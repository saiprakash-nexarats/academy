package com.nexarats.academy.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmAvatarUploadRequest(
        @NotBlank(message = "storageObjectKey is required")
        String storageObjectKey,
        String originalFilename
) {
}

