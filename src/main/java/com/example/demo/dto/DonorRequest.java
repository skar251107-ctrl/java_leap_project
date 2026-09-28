package com.example.demo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DonorRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,15}$", message = "Invalid phone number") String phone,
        @NotBlank(message = "Blood group is required") String bloodGroup,
        @NotBlank(message = "City is required") @Size(max = 100) String city,
        @PastOrPresent(message = "Last donation date cannot be in the future") LocalDate lastDonationDate) {
}