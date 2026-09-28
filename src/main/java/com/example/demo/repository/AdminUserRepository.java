package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsernameIgnoreCase(String username);
    Optional<AdminUser> findByEmailIgnoreCase(String email);
}