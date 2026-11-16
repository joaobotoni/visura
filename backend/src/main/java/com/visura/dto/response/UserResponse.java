package com.visura.dto.response;

import com.visura.vo.Cpf;
import com.visura.vo.Email;
import com.visura.vo.Name;

import java.util.UUID;

public record UserResponse(
        UUID id,
        Name name,
        Email email,
        Cpf cpf
) {
}
