package com.visura.dto.response;

public record AuthenticationResponse(
        UserResponse user,
        TokenResponse token
) {
}
