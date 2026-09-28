package com.example.demo.exception;

public class EntityInUseException extends RuntimeException {
    public EntityInUseException(String message) {
        super(message);
    }
}