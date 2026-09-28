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

import com.example.demo.dto.AdminUserRequest;
import com.example.demo.dto.AdminUserResponse;
import com.example.demo.dto.DeleteResponse;
import com.example.demo.service.AdminUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin-users")
@Validated
@Tag(name = "Admin users")
public class AdminUserController {
    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create an admin user; password is BCrypt-hashed")
    @ApiResponse(responseCode = "201", description = "Admin user created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public ResponseEntity<AdminUserResponse> create(@Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    @Operation(summary = "List admin users without password hashes")
    public List<AdminUserResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an admin user by ID without password hash")
    @ApiResponse(responseCode = "404", description = "Admin user does not exist")
    public AdminUserResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an admin user; omit password to keep the current hash")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Admin user does not exist")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public AdminUserResponse update(@PathVariable Long id, @Valid @RequestBody AdminUserRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an admin user")
    @ApiResponse(responseCode = "404", description = "Admin user does not exist")
    public DeleteResponse delete(@PathVariable Long id) {
        service.delete(id);
        return new DeleteResponse(200, "Admin user ID " + id + " deleted successfully.");
    }
}