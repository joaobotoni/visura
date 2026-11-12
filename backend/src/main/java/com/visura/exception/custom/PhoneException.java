package com.visura.exception.custom;

import com.visura.exception.enums.problem.PhoneProblem;

public final class PhoneException extends RuntimeException {

    private final PhoneProblem problem;

    public PhoneException(PhoneProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public PhoneProblem problem() {
        return problem;
    }
}
