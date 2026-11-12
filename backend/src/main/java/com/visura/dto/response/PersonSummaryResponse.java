package com.visura.dto.response;

import com.visura.database.enums.PersonType;

import java.util.UUID;

public record PersonSummaryResponse(
        UUID id,
        PersonType type,
        String name,
        String document
) {
}
