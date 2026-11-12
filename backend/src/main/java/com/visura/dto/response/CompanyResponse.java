package com.visura.dto.response;

import com.visura.database.enums.MembershipRole;

import java.util.UUID;

/**
 * Empresa, com o papel do usuário que pergunta.
 */
public record CompanyResponse(
        UUID id,
        String name,
        String tradeName,
        String cnpj,
        MembershipRole role
) {
}
