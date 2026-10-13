package com.nexarats.academy.identity.dto;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String avatarUrl
) {}
