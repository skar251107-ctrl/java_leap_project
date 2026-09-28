package com.example.demo.exception;

public class AdminUserNotFoundException extends ResourceNotFoundException {
    public AdminUserNotFoundException(Long id) {
        super("ADMIN_USER_NOT_FOUND", "Admin user ID " + id + " does not exist.");
    }
}