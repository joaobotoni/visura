package com.visura.exception.enums.problem;

import com.visura.exception.lib.problem.Problem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CepProblem implements Problem {

    MISSING(HttpStatus.BAD_REQUEST, "O CEP é obrigatório."),
    FORMAT(HttpStatus.BAD_REQUEST, "O CEP deve conter exatamente %d dígitos numéricos."),
    ZEROS(HttpStatus.BAD_REQUEST, "O CEP informado é inválido.");

    private final HttpStatus status;
    private final String message;
}
