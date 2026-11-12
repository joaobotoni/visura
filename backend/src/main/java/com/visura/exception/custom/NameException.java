package com.visura.exception.custom;

import com.visura.exception.enums.problem.NameProblem;

public final class NameException extends IllegalArgumentException {

    private final NameProblem problem;

    public NameException(NameProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public NameProblem problem() {
        return problem;
    }
}
