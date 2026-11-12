package com.visura.dto.request;

import com.visura.lib.Texts;
import com.visura.vo.Cnpj;
import com.visura.vo.Email;
import com.visura.vo.Phone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Pessoa jurídica do cadastro do usuário. {@code name} é a razão social; telefone e e-mail são opcionais.
 */
public record CompanyRequest(
        @NotBlank(message = "A razão social é obrigatória.")
        @Size(max = 200, message = "A razão social deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "A razão social contém caracteres inválidos.")
        String name,

        @NotBlank(message = "O nome fantasia é obrigatório.")
        @Size(max = 200, message = "O nome fantasia deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "O nome fantasia contém caracteres inválidos.")
        String tradeName,

        @NotNull(message = "O CNPJ é obrigatório.")
        Cnpj cnpj,

        Phone phone,

        Email email
) {

    public CompanyRequest {
        name = Texts.clean(name);
        tradeName = Texts.clean(tradeName);
    }
}
