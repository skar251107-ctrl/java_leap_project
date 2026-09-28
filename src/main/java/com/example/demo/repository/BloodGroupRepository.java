package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.BloodGroup;

public interface BloodGroupRepository extends JpaRepository<BloodGroup, Long> {
    Optional<BloodGroup> findByBloodGroup(String bloodGroup);
    boolean existsByBloodGroup(String bloodGroup);
}