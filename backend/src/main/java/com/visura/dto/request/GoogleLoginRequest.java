package com.visura.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequest(
        @NotBlank(message = "O token do Google é obrigatório.")
        @Size(max = 4096, message = "O token do Google é inválido.")
        String idToken,

        @Valid
        @NotNull(message = "Os dados do dispositivo são obrigatórios.")
        DeviceRequest device
) {
}
