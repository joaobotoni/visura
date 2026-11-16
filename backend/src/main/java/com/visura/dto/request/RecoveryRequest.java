package com.visura.dto.request;

import com.visura.vo.Email;
import jakarta.validation.constraints.NotNull;

public record RecoveryRequest(
        @NotNull(message = "O e-mail é obrigatório.")
        Email email
) {
}
