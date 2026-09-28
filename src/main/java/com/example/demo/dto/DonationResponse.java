package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DonationResponse(Long id, Long donorId, String donorName, LocalDate donationDate,
        String hospitalName, LocalDateTime createdAt) {
}