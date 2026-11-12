package com.visura.exception.handler;

import com.visura.exception.custom.PhotoException;
import com.visura.exception.lib.problem.Problems;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class PhotoExceptionHandler {

    @ExceptionHandler(PhotoException.class)
    ProblemDetail handle(PhotoException exception) {
        return Problems.of(exception, exception.problem()).build();
    }
}
