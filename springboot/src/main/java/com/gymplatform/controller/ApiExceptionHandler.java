package com.gymplatform.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail responseStatus(ResponseStatusException exception) {
        return ProblemDetail.forStatusAndDetail(
                exception.getStatusCode(),
                exception.getReason() == null ? "Request failed" : exception.getReason()
        );
    }
}
