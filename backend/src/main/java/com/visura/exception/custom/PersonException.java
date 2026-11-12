package com.visura.exception.custom;

import com.visura.exception.enums.problem.PersonProblem;

public final class PersonException extends RuntimeException {

    private final PersonProblem problem;

    public PersonException(PersonProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PersonProblem problem() {
        return problem;
    }
}
