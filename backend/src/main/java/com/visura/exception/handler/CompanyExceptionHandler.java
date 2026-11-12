package com.visura.exception.handler;

import com.visura.exception.custom.CompanyException;
import com.visura.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CompanyExceptionHandler {

    @ExceptionHandler(CompanyException.class)
    ProblemDetail handle(CompanyException exception) {
        return Problems.of(exception, exception.problem()).build();
    }
}
