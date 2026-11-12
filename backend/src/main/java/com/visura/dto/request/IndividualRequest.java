package com.visura.dto.request;

import com.visura.vo.Cpf;
import com.visura.vo.Email;
import com.visura.vo.Name;
import com.visura.vo.Phone;
import jakarta.validation.constraints.NotNull;

/**
 * Pessoa física do cadastro do usuário (proprietário, inquilino). Telefone e e-mail são opcionais.
 */
public record IndividualRequest(
        @NotNull(message = "O nome é obrigatório.")
        Name name,

        @NotNull(message = "O CPF é obrigatório.")
        Cpf cpf,

        Phone phone,

        Email email
) {
}
