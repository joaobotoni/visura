package com.visura.exception.custom;

import com.visura.exception.enums.problem.CepProblem;

public final class CepException extends RuntimeException {

    private final CepProblem problem;

    public CepException(CepProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CepProblem problem() {
        return problem;
    }
}
