package com.visura.exception.custom;

import com.visura.exception.enums.problem.JwtProblem;

public final class JwtException extends RuntimeException {

    private final JwtProblem problem;

    public JwtException(JwtProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public JwtProblem problem() {
        return problem;
    }
}
