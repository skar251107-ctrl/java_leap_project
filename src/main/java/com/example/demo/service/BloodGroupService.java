package com.example.demo.service;

import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.BloodGroupRequest;
import com.example.demo.dto.BloodGroupResponse;
import com.example.demo.dto.BloodGroupCountResponse;
import com.example.demo.entity.BloodGroup;
import com.example.demo.exception.BloodGroupNotFoundException;
import com.example.demo.exception.EntityInUseException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UniqueConflictException;
import com.example.demo.repository.BloodGroupRepository;
import com.example.demo.repository.DonorRepository;

@Service
public class BloodGroupService implements CommandLineRunner {
    private static final List<String> STANDARD_GROUPS = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private static final Set<String> ALLOWED_GROUPS = Set.copyOf(STANDARD_GROUPS);

    private final BloodGroupRepository bloodGroupRepository;
    private final DonorRepository donorRepository;

    public BloodGroupService(BloodGroupRepository bloodGroupRepository, DonorRepository donorRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
        this.donorRepository = donorRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (String bloodGroup : STANDARD_GROUPS) {
            if (!bloodGroupRepository.existsByBloodGroup(bloodGroup)) {
                bloodGroupRepository.save(new BloodGroup(bloodGroup, null));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<BloodGroupResponse> getAll() {
        return bloodGroupRepository.findAll().stream()
                .sorted((left, right) -> left.getBloodGroup().compareTo(right.getBloodGroup()))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BloodGroupCountResponse> getCounts() {
        return bloodGroupRepository.findAll().stream()
                .sorted((left, right) -> left.getBloodGroup().compareTo(right.getBloodGroup()))
                .map(group -> new BloodGroupCountResponse(group.getBloodGroup(), donorRepository.countByBloodGroup_Id(group.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BloodGroupResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public BloodGroupResponse create(BloodGroupRequest request) {
        validateAllowed(request.bloodGroup());
        if (bloodGroupRepository.existsByBloodGroup(request.bloodGroup())) {
            throw new UniqueConflictException("Blood group " + request.bloodGroup() + " already exists.");
        }
        return toResponse(bloodGroupRepository.save(new BloodGroup(request.bloodGroup(), clean(request.description()))));
    }

    @Transactional
    public BloodGroupResponse update(Long id, BloodGroupRequest request) {
        BloodGroup group = findById(id);
        validateAllowed(request.bloodGroup());
        bloodGroupRepository.findByBloodGroup(request.bloodGroup())
                .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> { throw new UniqueConflictException("Blood group " + request.bloodGroup() + " already exists."); });
        group.setBloodGroup(request.bloodGroup());
        group.setDescription(clean(request.description()));
        return toResponse(bloodGroupRepository.save(group));
    }

    @Transactional
    public void delete(Long id) {
        BloodGroup group = findById(id);
        if (donorRepository.existsByBloodGroup_Id(id)) {
            throw new EntityInUseException("Blood group " + group.getBloodGroup() + " is assigned to donors and cannot be deleted.");
        }
        bloodGroupRepository.delete(group);
    }

    @Transactional(readOnly = true)
    public BloodGroup requireByValue(String value) {
        return bloodGroupRepository.findByBloodGroup(value)
                .orElseThrow(() -> new ResourceNotFoundException("BLOOD_GROUP_NOT_FOUND", "Blood group " + value + " does not exist."));
    }

    private BloodGroup findById(Long id) {
        return bloodGroupRepository.findById(id).orElseThrow(() -> new BloodGroupNotFoundException(id));
    }

    private void validateAllowed(String bloodGroup) {
        if (!ALLOWED_GROUPS.contains(bloodGroup)) {
            throw new IllegalArgumentException("Blood group must be one of A+, A-, B+, B-, AB+, AB-, O+, or O-.");
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BloodGroupResponse toResponse(BloodGroup group) {
        return new BloodGroupResponse(group.getId(), group.getBloodGroup(), group.getDescription());
    }
}