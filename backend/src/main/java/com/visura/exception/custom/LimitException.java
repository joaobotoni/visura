package com.visura.exception.custom;

import com.visura.exception.enums.problem.LimitProblem;

public final class LimitException extends IllegalArgumentException {

    private final LimitProblem problem;

    public LimitException(LimitProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public LimitProblem problem() {
        return problem;
    }
}
