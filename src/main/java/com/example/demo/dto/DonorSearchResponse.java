package com.example.demo.dto;

import java.time.LocalDate;

public record DonorSearchResponse(Long id, String name, String bloodGroup, String city,
        LocalDate lastDonationDate, boolean available) {
}