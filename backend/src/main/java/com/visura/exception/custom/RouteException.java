package com.visura.exception.custom;

import com.visura.exception.enums.problem.RouteProblem;

public final class RouteException extends IllegalArgumentException {

    private final RouteProblem problem;

    public RouteException(RouteProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public RouteProblem problem() {
        return problem;
    }
}
