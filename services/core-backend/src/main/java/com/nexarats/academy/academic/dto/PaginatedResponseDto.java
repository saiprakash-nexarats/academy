package com.nexarats.academy.academic.dto;

import java.util.List;

public record PaginatedResponseDto<T>(
        List<T> items,
        long total,
        int page,
        int size,
        int totalPages
) {
    public static <T> PaginatedResponseDto<T> of(List<T> items, long total, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PaginatedResponseDto<>(items, total, page, size, totalPages);
    }
}
