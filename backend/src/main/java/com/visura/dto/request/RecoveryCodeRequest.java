package com.visura.dto.request;

import com.visura.vo.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RecoveryCodeRequest(
        @NotNull(message = "O e-mail é obrigatório.")
        Email email,

        @NotNull(message = "O código é obrigatório.")
        @Pattern(regexp = "\\d{6}", message = "O código tem 6 dígitos.")
        String code
) {
}
