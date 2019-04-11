package com.project.gva.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.Charset;

public class RestResponseException extends RestClientResponseException {

    private final HttpStatus statusCode;

    public RestResponseException(HttpStatus statusCode) {
        this(statusCode, statusCode.name(), null, null, null);
    }

    public RestResponseException(HttpStatus statusCode, String statusText) {
        this(statusCode, statusText, null, null, null);
    }

    public RestResponseException(HttpStatus statusCode, String statusText, byte[] responseBody, Charset responseCharset) {
        this(statusCode, statusText, null, responseBody, responseCharset);
    }

    public RestResponseException(HttpStatus statusCode, String statusText, HttpHeaders responseHeaders, byte[] responseBody, Charset responseCharset) {
        super(statusCode.value() + " " + statusText, statusCode.value(), statusText, responseHeaders, responseBody, responseCharset);
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return this.statusCode;
    }
}

