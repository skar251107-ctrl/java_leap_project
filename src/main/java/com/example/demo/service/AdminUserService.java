package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AdminUserRequest;
import com.example.demo.dto.AdminUserResponse;
import com.example.demo.entity.AdminUser;
import com.example.demo.exception.AdminUserNotFoundException;
import com.example.demo.exception.RequestValidationException;
import com.example.demo.exception.UniqueConflictException;
import com.example.demo.repository.AdminUserRepository;

@Service
public class AdminUserService {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminUserResponse create(AdminUserRequest request) {
        if (request.password() == null || request.password().isBlank()) {
            throw new RequestValidationException(Map.of("password", "Password is required when creating an admin user."));
        }
        ensureUnique(request, null);
        AdminUser user = new AdminUser();
        apply(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return toResponse(adminUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAll() {
        return adminUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserRequest request) {
        AdminUser user = findById(id);
        ensureUnique(request, id);
        apply(user, request);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return toResponse(adminUserRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        adminUserRepository.delete(findById(id));
    }

    private AdminUser findById(Long id) {
        return adminUserRepository.findById(id).orElseThrow(() -> new AdminUserNotFoundException(id));
    }

    private void ensureUnique(AdminUserRequest request, Long id) {
        adminUserRepository.findByUsernameIgnoreCase(request.username().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new UniqueConflictException("Username already exists."); });
        if (request.email() != null && !request.email().isBlank()) {
            adminUserRepository.findByEmailIgnoreCase(request.email().trim())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new UniqueConflictException("Email already exists."); });
        }
    }

    private void apply(AdminUser user, AdminUserRequest request) {
        user.setUsername(request.username().trim());
        user.setEmail(request.email() == null || request.email().isBlank() ? null : request.email().trim().toLowerCase());
        user.setRole(request.role().trim());
    }

    private AdminUserResponse toResponse(AdminUser user) {
        return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}