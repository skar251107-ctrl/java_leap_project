package com.example.demo.exception;

public class CooldownException extends RuntimeException {
    public CooldownException(String message) {
        super(message);
    }
}