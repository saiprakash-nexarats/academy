package com.nexarats.academy.identity.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SwitchTenantRequest(
        @NotNull(message = "Target tenant ID is required")
        UUID tenantId
) {}
