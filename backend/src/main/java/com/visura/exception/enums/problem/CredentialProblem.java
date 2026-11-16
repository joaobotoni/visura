package com.visura.exception.enums.problem;

import com.visura.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CredentialProblem implements Problem {

    NOT_FOUND(HttpStatus.NOT_FOUND, "A credencial informada não foi encontrada."),
    INCORRECT_CURRENT_PASSWORD(HttpStatus.UNPROCESSABLE_CONTENT, "A senha atual informada está incorreta."),
    SAME_PASSWORD(HttpStatus.UNPROCESSABLE_CONTENT, "A nova senha deve ser diferente da senha atual."),
    INVALID_RECOVERY_CODE(HttpStatus.UNPROCESSABLE_CONTENT, "O código está incorreto ou venceu. Peça um código novo."),
    RECOVERY_USED(HttpStatus.CONFLICT, "Este código já foi usado. Peça um código novo."),
    GOOGLE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "O login com Google não está configurado no servidor."),
    GOOGLE_INVALID(HttpStatus.UNAUTHORIZED, "Não foi possível confirmar a sua conta do Google."),
    GOOGLE_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Nenhuma conta do Visura usa este e-mail do Google. Crie a sua conta primeiro.");

    private final HttpStatus status;
    private final String message;
}
