package com.visura.dto.response;

import java.util.UUID;

/**
 * O usuário responsável pela vistoria: numa empresa, pode ser outro membro.
 */
public record ResponsibleResponse(
        UUID id,
        String name
) {
}
