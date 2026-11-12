package com.visura.dto.request;

import com.visura.vo.Cpf;
import com.visura.vo.Email;
import com.visura.vo.Name;
import com.visura.vo.NewPassword;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull(message = "O nome é obrigatório.")
        Name name,

        @NotNull(message = "O CPF é obrigatório.")
        Cpf cpf,

        @NotNull(message = "O e-mail é obrigatório.")
        Email email,

        @NotNull(message = "A senha é obrigatória.")
        NewPassword password,

        @Valid
        @NotNull(message = "Os dados do dispositivo são obrigatórios.")
        DeviceRequest device
) {
}
