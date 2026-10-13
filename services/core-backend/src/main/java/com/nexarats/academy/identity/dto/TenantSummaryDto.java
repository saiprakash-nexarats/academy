package com.nexarats.academy.identity.dto;

import java.util.UUID;

public record TenantSummaryDto(
        UUID id,
        String slug,
        String name,
        String status
) {}
