package com.visura.exception.custom;

import com.visura.exception.enums.problem.CnpjProblem;

public final class CnpjException extends IllegalArgumentException {

    private final CnpjProblem problem;

    public CnpjException(CnpjProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CnpjProblem problem() {
        return problem;
    }
}
