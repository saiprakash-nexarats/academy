package com.nexarats.academy.identity.dto;

public record TokenRefreshResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken
) {
    public TokenRefreshResponse(String accessToken, long expiresIn, String refreshToken) {
        this(accessToken, "Bearer", expiresIn, refreshToken);
    }
}
