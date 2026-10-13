package com.nexarats.academy.academic.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProgramDetailDto(
        UUID id,
        String slug,
        String title,
        String shortDescription,
        String detailedDescription,
        String imageAssetKey,
        int durationValue,
        String durationUnit,
        String durationLabel,
        String level,
        String badge,
        BigDecimal rating,
        String categorySlug,
        String categoryName,
        String curriculumOverview,
        String careerOutcomes,
        boolean isFeatured,
        int displayOrder,
        Instant publishedAt
) {
}
