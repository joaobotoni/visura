package com.visura.exception.custom;

import com.visura.exception.enums.problem.CredentialProblem;

public final class CredentialException extends RuntimeException {

    private final CredentialProblem problem;

    public CredentialException(CredentialProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public CredentialProblem problem() {
        return problem;
    }
}
