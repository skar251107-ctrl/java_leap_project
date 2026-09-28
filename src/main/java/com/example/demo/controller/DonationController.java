package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DeleteResponse;
import com.example.demo.dto.DonationRequest;
import com.example.demo.dto.DonationResponse;
import com.example.demo.service.DonationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/donations")
@Validated
@Tag(name = "Donations")
public class DonationController {
    private final DonationService service;

    public DonationController(DonationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Record a blood donation")
    @ApiResponse(responseCode = "201", description = "Donation recorded")
    @ApiResponse(responseCode = "400", description = "Validation error or 90-day cooldown")
    @ApiResponse(responseCode = "404", description = "Donor does not exist")
    public ResponseEntity<DonationResponse> create(@Valid @RequestBody DonationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "List donation records")
    public List<DonationResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a donation record by ID")
    @ApiResponse(responseCode = "404", description = "Donation record does not exist")
    public DonationResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a donation record")
    @ApiResponse(responseCode = "400", description = "Validation error or 90-day cooldown")
    @ApiResponse(responseCode = "404", description = "Donation record or donor does not exist")
    public DonationResponse update(@PathVariable Long id, @Valid @RequestBody DonationRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a donation record")
    @ApiResponse(responseCode = "404", description = "Donation record does not exist")
    public DeleteResponse delete(@PathVariable Long id) {
        service.delete(id);
        return new DeleteResponse(200, "Donation record ID " + id + " deleted successfully.");
    }
}