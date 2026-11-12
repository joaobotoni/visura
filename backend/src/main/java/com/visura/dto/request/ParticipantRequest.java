package com.visura.dto.request;

import com.visura.database.enums.ParticipantRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ParticipantRequest(
        @NotNull(message = "A pessoa é obrigatória.")
        UUID person,

        @NotNull(message = "O papel da pessoa na vistoria é obrigatório.")
        ParticipantRole role
) {
}
