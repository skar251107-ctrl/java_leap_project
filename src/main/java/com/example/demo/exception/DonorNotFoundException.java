package com.example.demo.exception;

public class DonorNotFoundException extends ResourceNotFoundException {
    public DonorNotFoundException(Long id) {
        super("DONOR_NOT_FOUND", "Donor ID " + id + " does not exist.");
    }
}