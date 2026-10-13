package com.nexarats.academy.academic.dto;

import java.util.UUID;

public record CourseProgramRefDto(
        UUID id,
        String slug,
        String title
) {}
