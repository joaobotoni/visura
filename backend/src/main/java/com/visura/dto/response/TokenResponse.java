package com.visura.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
