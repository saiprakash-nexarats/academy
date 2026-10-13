package com.nexarats.academy.academic.dto;

import java.util.UUID;

public record LessonSummaryDto(
        UUID id,
        String title,
        String summary,
        String lessonType,
        int durationMinutes,
        boolean isPreviewable,
        int displayOrder,
        boolean isLocked
) {}
