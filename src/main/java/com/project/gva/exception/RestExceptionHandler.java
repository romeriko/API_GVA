package com.project.gva.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {RestResponseException.class})
    protected ResponseEntity<Object> handler(RestResponseException e, WebRequest request) {
        return handleExceptionInternal(e, Error.builder().message(e.getMessage()).status(e.getStatusCode()).build(),
                new HttpHeaders(), e.getStatusCode(), request);
    }
}