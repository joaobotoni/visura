package com.visura.dto.response;

import java.util.UUID;

public record ItemTypeResponse(
        UUID id,
        String name,
        boolean standard
) {
}
