package com.example.demo.dto;

import java.time.LocalDateTime;

public record AdminUserResponse(Long id, String username, String email, String role, LocalDateTime createdAt) {
}