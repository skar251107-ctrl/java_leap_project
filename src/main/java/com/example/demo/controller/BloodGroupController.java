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

import com.example.demo.dto.BloodGroupCountResponse;
import com.example.demo.dto.BloodGroupRequest;
import com.example.demo.dto.BloodGroupResponse;
import com.example.demo.dto.DeleteResponse;
import com.example.demo.service.BloodGroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blood-groups")
@Validated
@Tag(name = "Blood groups")
public class BloodGroupController {
    private final BloodGroupService service;

    public BloodGroupController(BloodGroupService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a blood group")
    @ApiResponse(responseCode = "201", description = "Blood group created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "409", description = "Blood group already exists")
    public ResponseEntity<BloodGroupResponse> create(@Valid @RequestBody BloodGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "List blood groups")
    public List<BloodGroupResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/count")
    @Operation(summary = "Count donors in each blood group")
    public List<BloodGroupCountResponse> getCounts() {
        return service.getCounts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a blood group by ID")
    @ApiResponse(responseCode = "404", description = "Blood group does not exist")
    public BloodGroupResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a blood group")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Blood group does not exist")
    @ApiResponse(responseCode = "409", description = "Blood group already exists")
    public BloodGroupResponse update(@PathVariable Long id, @Valid @RequestBody BloodGroupRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a blood group")
    @ApiResponse(responseCode = "200", description = "Blood group deleted")
    @ApiResponse(responseCode = "404", description = "Blood group does not exist")
    @ApiResponse(responseCode = "409", description = "Blood group is assigned to donors")
    public DeleteResponse delete(@PathVariable Long id) {
        service.delete(id);
        return new DeleteResponse(200, "Blood group ID " + id + " deleted successfully.");
    }
}