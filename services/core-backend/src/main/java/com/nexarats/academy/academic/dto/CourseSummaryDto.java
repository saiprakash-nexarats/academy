package com.nexarats.academy.academic.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CourseSummaryDto(
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
        List<String> techStack,
        boolean isFeatured,
        int displayOrder
) {}
