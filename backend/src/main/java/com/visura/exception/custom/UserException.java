package com.visura.exception.custom;

import com.visura.exception.enums.problem.UserProblem;

public final class UserException extends RuntimeException {

    private final UserProblem problem;

    public UserException(UserProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public UserProblem problem() {
        return problem;
    }
}
