package com.visura.dto.request;

import com.visura.vo.NewPassword;
import com.visura.vo.Password;
import jakarta.validation.constraints.NotNull;

public record ChangePasswordRequest(
        @NotNull(message = "A senha atual é obrigatória.")
        Password currentPassword,

        @NotNull(message = "A nova senha é obrigatória.")
        NewPassword newPassword
) {
}
