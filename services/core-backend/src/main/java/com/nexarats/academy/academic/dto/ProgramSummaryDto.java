package com.nexarats.academy.academic.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProgramSummaryDto(
        UUID id,
        String slug,
        String title,
        String shortDescription,
        String imageAssetKey,
        int durationValue,
        String durationUnit,
        String durationLabel,
        String level,
        String badge,
        BigDecimal rating,
        String categorySlug,
        String categoryName,
        boolean isFeatured,
        int displayOrder
) {
}
