package com.visura.dto.request;

import com.visura.vo.Email;
import com.visura.vo.Password;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "O e-mail é obrigatório.")
        Email email,

        @NotNull(message = "A senha é obrigatória.")
        Password password,

        @Valid
        @NotNull(message = "Os dados do dispositivo são obrigatórios.")
        DeviceRequest device
) {
}
