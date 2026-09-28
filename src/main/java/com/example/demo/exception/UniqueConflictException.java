package com.example.demo.exception;

public class UniqueConflictException extends RuntimeException {
    public UniqueConflictException(String message) {
        super(message);
    }
}