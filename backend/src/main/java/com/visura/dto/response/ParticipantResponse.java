package com.visura.dto.response;

import com.visura.database.enums.ParticipantRole;

public record ParticipantResponse(
        PersonSummaryResponse person,
        ParticipantRole role
) {
}
