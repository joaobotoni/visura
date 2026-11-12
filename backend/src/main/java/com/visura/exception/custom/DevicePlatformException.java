package com.visura.exception.custom;

import com.visura.exception.enums.problem.DevicePlatformProblem;

public final class DevicePlatformException extends RuntimeException {

    private final DevicePlatformProblem problem;

    public DevicePlatformException(DevicePlatformProblem problem, Object... args) {
        super(String.format(problem.message(), args));
        this.problem = problem;
    }

    public DevicePlatformProblem problem() {
        return problem;
    }
}
