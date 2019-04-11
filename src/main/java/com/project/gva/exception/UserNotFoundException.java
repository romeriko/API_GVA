package com.project.gva.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User not found")
public class UserNotFoundException extends RestResponseException {

    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User Not Found");
    }
}
