package com.visura.dto.response;

import com.visura.database.enums.MembershipRole;
import com.visura.vo.Email;

import java.util.UUID;

/**
 * Membro da empresa: {@code id} é o UUID público do usuário.
 */
public record MemberResponse(
        UUID id,
        String name,
        Email email,
        MembershipRole role
) {
}
