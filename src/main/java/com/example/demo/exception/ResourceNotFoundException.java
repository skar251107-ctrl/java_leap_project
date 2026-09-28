package com.example.demo.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}