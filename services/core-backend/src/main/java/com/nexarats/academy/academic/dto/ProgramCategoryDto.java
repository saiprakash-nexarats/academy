package com.nexarats.academy.academic.dto;

import java.util.UUID;

public record ProgramCategoryDto(
        UUID id,
        String slug,
        String name,
        String description,
        int displayOrder
) {
}
