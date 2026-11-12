package com.visura.exception.custom;

import com.visura.exception.enums.problem.CpfProblem;

public final class CpfException extends IllegalArgumentException {

    private final CpfProblem problem;

    public CpfException(CpfProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CpfProblem problem() {
        return problem;
    }
}
