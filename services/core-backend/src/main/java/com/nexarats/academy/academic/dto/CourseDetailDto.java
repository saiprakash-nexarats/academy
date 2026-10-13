package com.nexarats.academy.academic.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseDetailDto(
        UUID id,
        String slug,
        String title,
        String category,
        String level,
        int durationWeeks,
        int lessonsCount,
        int projectsCount,
        BigDecimal rating,
        int reviewsCount,
        String enrolledStudents,
        CourseInstructorDto instructor,
        String imageAssetKey,
        String badge,
        String shortDescription,
        String detailedDescription,
        List<String> techStack,
        List<String> learningOutcomes,
        String prerequisites,
        String publishedVersionLabel,
        int modulesCount,
        List<CourseProgramRefDto> relatedPrograms,
        boolean isFeatured,
        int displayOrder,
        Instant publishedAt
) {}
