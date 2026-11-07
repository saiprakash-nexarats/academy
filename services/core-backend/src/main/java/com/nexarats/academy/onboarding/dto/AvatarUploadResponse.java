package com.nexarats.academy.onboarding.dto;

import com.nexarats.academy.onboarding.model.ScanStatus;

import java.util.UUID;

public record AvatarUploadResponse(
        UUID avatarAssetId,
        String storageObjectKey,
        String contentType,
        long fileSizeBytes,
        ScanStatus scanStatus,
        String scanDetails,
        String avatarUrl
) {
}
