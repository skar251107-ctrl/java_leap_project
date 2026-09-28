package com.example.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DonorResponse(Long id, String name, String phone, String bloodGroup, String city,
        LocalDate lastDonationDate, boolean available, LocalDateTime createdAt) {
}