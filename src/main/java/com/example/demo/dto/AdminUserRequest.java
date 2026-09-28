package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUserRequest(
        @NotBlank(message = "Username is required") @Size(max = 50) String username,
        @Email(message = "Email must be valid") @Size(max = 150) String email,
        @Size(min = 12, max = 200, message = "Password must contain at least 12 characters") String password,
        @NotBlank(message = "Role is required") @Size(max = 30) String role) {
}