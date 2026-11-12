package com.visura.dto.response;

import com.visura.database.enums.InspectionStatus;
import com.visura.database.enums.InspectionType;

import java.time.Instant;
import java.util.UUID;

public record InspectionResponse(
        UUID id,
        UUID property,
        InspectionType type,
        InspectionStatus status,
        UUID entry,
        String notes,
        Instant startedAt,
        Instant finishedAt
) {
}
