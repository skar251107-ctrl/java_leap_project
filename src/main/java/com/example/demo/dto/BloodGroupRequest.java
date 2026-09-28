package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BloodGroupRequest(
        @NotBlank(message = "Blood group is required")
        @Pattern(regexp = "^(A[+-]|B[+-]|AB[+-]|O[+-])$", message = "Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, or O-")
        String bloodGroup,
        @Size(max = 255, message = "Description cannot exceed 255 characters") String description) {
}