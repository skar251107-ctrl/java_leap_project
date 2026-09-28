package com.example.demo.exception;

public class DonationNotFoundException extends ResourceNotFoundException {
    public DonationNotFoundException(Long id) {
        super("DONATION_NOT_FOUND", "Donation record ID " + id + " does not exist.");
    }
}