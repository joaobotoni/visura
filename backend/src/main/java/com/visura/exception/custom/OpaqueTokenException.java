package com.visura.exception.custom;

import com.visura.exception.enums.problem.OpaqueTokenProblem;

public final class OpaqueTokenException extends RuntimeException {

    private final OpaqueTokenProblem problem;

    public OpaqueTokenException(OpaqueTokenProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public OpaqueTokenProblem problem() {
        return problem;
    }
}
