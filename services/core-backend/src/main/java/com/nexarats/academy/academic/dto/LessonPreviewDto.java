package com.nexarats.academy.academic.dto;

import java.util.UUID;

public record LessonPreviewDto(
        UUID lessonId,
        String courseSlug,
        String moduleNumber,
        String title,
        String summary,
        String lessonType,
        int durationMinutes,
        String previewContent
) {}
