package com.nexarats.academy.identity.dto;

import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String avatarUrl,
        String status,
        boolean emailVerified,
        TenantSummaryDto tenant,
        List<String> roles,
        List<String> permissions,
        List<TenantSummaryDto> availableTenants
) {}
