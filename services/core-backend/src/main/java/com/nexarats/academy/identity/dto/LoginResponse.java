package com.nexarats.academy.identity.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummaryDto user,
        TenantSummaryDto tenant,
        List<String> roles,
        List<String> permissions,
        String refreshToken
) {
    public LoginResponse(
            String accessToken,
            long expiresIn,
            UserSummaryDto user,
            TenantSummaryDto tenant,
            List<String> roles,
            List<String> permissions,
            String refreshToken
    ) {
        this(accessToken, "Bearer", expiresIn, user, tenant, roles, permissions, refreshToken);
    }
}
