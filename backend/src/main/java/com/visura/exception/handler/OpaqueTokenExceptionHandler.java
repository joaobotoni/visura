package com.visura.exception.handler;

import com.visura.exception.custom.OpaqueTokenException;
import com.visura.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class OpaqueTokenExceptionHandler {

    @ExceptionHandler(OpaqueTokenException.class)
    ProblemDetail handle(OpaqueTokenException exception) {
        return Problems.of(exception, exception.problem()).build();
    }
}
