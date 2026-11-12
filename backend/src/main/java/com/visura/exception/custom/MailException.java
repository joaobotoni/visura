package com.visura.exception.custom;

import com.visura.exception.enums.problem.MailProblem;

public final class MailException extends IllegalArgumentException {

    private final MailProblem problem;

    public MailException(MailProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public MailProblem problem() {
        return problem;
    }
}
