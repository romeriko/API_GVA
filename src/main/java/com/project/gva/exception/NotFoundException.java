package com.project.gva.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Object not found.")
public class NotFoundException extends RestResponseException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NotFoundException() {
        super(HttpStatus.NOT_FOUND, "Object could not be found");
    }
}
