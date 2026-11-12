package com.visura.exception.custom;

import com.visura.exception.enums.problem.RateLimitProblem;

public final class RateLimitException extends RuntimeException {

    private final RateLimitProblem problem;

    public RateLimitException(RateLimitProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public RateLimitProblem problem() {
        return problem;
    }
}
