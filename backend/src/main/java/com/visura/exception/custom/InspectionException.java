package com.visura.exception.custom;

import com.visura.exception.enums.problem.InspectionProblem;

public final class InspectionException extends RuntimeException {

    private final InspectionProblem problem;

    public InspectionException(InspectionProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public InspectionProblem problem() {
        return problem;
    }
}
