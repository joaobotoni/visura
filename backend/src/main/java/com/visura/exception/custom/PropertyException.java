package com.visura.exception.custom;

import com.visura.exception.enums.problem.PropertyProblem;

public final class PropertyException extends RuntimeException {

    private final PropertyProblem problem;

    public PropertyException(PropertyProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PropertyProblem problem() {
        return problem;
    }
}
