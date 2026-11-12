package com.visura.exception.handler;

import com.visura.exception.custom.RefreshTokenException;
import com.visura.exception.enums.problem.SecurityProblem;
import com.visura.exception.lib.problem.Problems;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RefreshTokenExceptionHandler {

    @ExceptionHandler(RefreshTokenException.class)
    ProblemDetail handle(RefreshTokenException exception, HttpServletRequest request) {
        log.warn("Refresh token recusado: motivo={} ip={}", exception.problem(), request.getRemoteAddr());
        return Problems.of(SecurityProblem.INVALID_SESSION).build();
    }
}
