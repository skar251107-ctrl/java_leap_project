package com.example.demo.exception;

public class BloodGroupNotFoundException extends ResourceNotFoundException {
    public BloodGroupNotFoundException(Long id) {
        super("BLOOD_GROUP_NOT_FOUND", "Blood group ID " + id + " does not exist.");
    }
}