package com.example.demo.exception;

public class InvalidDonationException extends RuntimeException {
    public InvalidDonationException(String message) {
        super(message);
    }
}