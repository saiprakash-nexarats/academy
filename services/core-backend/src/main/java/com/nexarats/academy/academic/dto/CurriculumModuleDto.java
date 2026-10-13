package com.nexarats.academy.academic.dto;

import java.util.List;
import java.util.UUID;

public record CurriculumModuleDto(
        UUID id,
        String number,
        String title,
        String duration,
        int displayOrder,
        int lessonsCount,
        List<String> topics,
        List<LessonSummaryDto> lessons
) {}
