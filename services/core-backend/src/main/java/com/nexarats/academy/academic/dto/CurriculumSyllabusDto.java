package com.nexarats.academy.academic.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CurriculumSyllabusDto(
        UUID courseId,
        String courseSlug,
        String courseTitle,
        String versionLabel,
        Instant publishedAt,
        int totalModules,
        int totalLessons,
        List<CurriculumModuleDto> modules
) {}
