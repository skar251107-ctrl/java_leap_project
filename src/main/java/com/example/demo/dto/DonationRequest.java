package com.example.demo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DonationRequest(
        @NotNull(message = "Donor is required") @Positive(message = "Donor ID must be positive") Long donorId,
        @NotNull(message = "Donation date is required") @PastOrPresent(message = "Donation date cannot be in the future") LocalDate donationDate,
        @NotBlank(message = "Hospital name is required") @Size(max = 150) String hospitalName) {
}