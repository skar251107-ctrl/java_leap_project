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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DeleteResponse;
import com.example.demo.dto.DonorRequest;
import com.example.demo.dto.DonorResponse;
import com.example.demo.dto.DonorSearchResponse;
import com.example.demo.service.DonorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/donors")
@Validated
@Tag(name = "Donors")
public class DonorController {
    private final DonorService service;

    public DonorController(DonorService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a donor")
    @ApiResponse(responseCode = "201", description = "Donor registered")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "409", description = "Phone number already registered")
    public ResponseEntity<DonorResponse> create(@Valid @RequestBody DonorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/search")
    @Operation(summary = "Search currently eligible donors; phone numbers are not returned")
    @ApiResponse(responseCode = "400", description = "Invalid search parameter")
    public List<DonorSearchResponse> search(@RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) String city) {
        return service.search(bloodGroup, city);
    }

    @GetMapping
    @Operation(summary = "List donors")
    public List<DonorResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a donor by ID")
    @ApiResponse(responseCode = "404", description = "Donor does not exist")
    public DonorResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a donor")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Donor does not exist")
    @ApiResponse(responseCode = "409", description = "Phone number already registered")
    public DonorResponse update(@PathVariable Long id, @Valid @RequestBody DonorRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a donor and their donation history")
    @ApiResponse(responseCode = "404", description = "Donor does not exist")
    public DeleteResponse delete(@PathVariable Long id) {
        service.delete(id);
        return new DeleteResponse(200, "Donor ID " + id + " deleted successfully.");
    }
}