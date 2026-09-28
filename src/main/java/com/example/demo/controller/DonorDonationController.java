package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DonationResponse;
import com.example.demo.service.DonationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/donors/{donorId}/donations")
@Tag(name = "Donations")
public class DonorDonationController {
    private final DonationService service;

    public DonorDonationController(DonationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get donation history for a donor")
    @ApiResponse(responseCode = "404", description = "Donor does not exist")
    public List<DonationResponse> getForDonor(@PathVariable Long donorId) {
        return service.getForDonor(donorId);
    }
}