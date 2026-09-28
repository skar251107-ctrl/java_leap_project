package com.example.demo.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.DonorRequest;
import com.example.demo.dto.DonorResponse;
import com.example.demo.dto.DonorSearchResponse;
import com.example.demo.entity.BloodGroup;
import com.example.demo.entity.Donor;
import com.example.demo.exception.DonorNotFoundException;
import com.example.demo.exception.UniqueConflictException;
import com.example.demo.repository.DonationRecordRepository;
import com.example.demo.repository.DonorRepository;

@Service
public class DonorService {
    private static final long COOLDOWN_DAYS = 90;
    private final DonorRepository donorRepository;
    private final DonationRecordRepository donationRecordRepository;
    private final BloodGroupService bloodGroupService;

    public DonorService(DonorRepository donorRepository, DonationRecordRepository donationRecordRepository,
            BloodGroupService bloodGroupService) {
        this.donorRepository = donorRepository;
        this.donationRecordRepository = donationRecordRepository;
        this.bloodGroupService = bloodGroupService;
    }

    @Transactional
    public DonorResponse create(DonorRequest request) {
        ensurePhoneAvailable(request.phone(), null);
        Donor donor = new Donor();
        apply(donor, request);
        return toResponse(donorRepository.save(donor));
    }

    @Transactional(readOnly = true)
    public List<DonorResponse> getAll() {
        return donorRepository.findAll().stream().map(this::toResponse)
            .sorted(Comparator.comparing((DonorResponse response) -> response.name(), String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Transactional(readOnly = true)
    public DonorResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public DonorResponse update(Long id, DonorRequest request) {
        Donor donor = findById(id);
        ensurePhoneAvailable(request.phone(), id);
        apply(donor, request);
        return toResponse(donorRepository.save(donor));
    }

    @Transactional
    public void delete(Long id) {
        Donor donor = findById(id);
        donationRecordRepository.deleteAll(donationRecordRepository.findByDonor_IdOrderByDonationDateDesc(id));
        donorRepository.delete(donor);
    }

    @Transactional(readOnly = true)
    public List<DonorSearchResponse> search(String bloodGroup, String city) {
        if (bloodGroup != null && !bloodGroup.isBlank()
                && !List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").contains(bloodGroup)) {
            throw new IllegalArgumentException("Unsupported blood group: " + bloodGroup);
        }
        String normalizedCity = city == null || city.isBlank() ? null : city.trim();
        LocalDate cutoff = LocalDate.now().minusDays(COOLDOWN_DAYS);
        return donorRepository.searchAvailable(bloodGroup, normalizedCity, cutoff).stream()
                .map(donor -> new DonorSearchResponse(donor.getId(), donor.getName(), donor.getBloodGroup().getBloodGroup(),
                        donor.getCity(), donor.getLastDonationDate(), true))
                .sorted(Comparator.comparing((DonorSearchResponse response) -> response.name(), String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Transactional(readOnly = true)
    public Donor findById(Long id) {
        return donorRepository.findById(id).orElseThrow(() -> new DonorNotFoundException(id));
    }

    private void apply(Donor donor, DonorRequest request) {
        donor.setName(request.name().trim());
        donor.setPhone(request.phone().trim());
        BloodGroup group = bloodGroupService.requireByValue(request.bloodGroup());
        donor.setBloodGroup(group);
        donor.setCity(request.city().trim());
        donor.setLastDonationDate(request.lastDonationDate());
        donor.setAvailable(isEligible(request.lastDonationDate()));
    }

    private void ensurePhoneAvailable(String phone, Long donorId) {
        donorRepository.findByPhone(phone.trim()).filter(existing -> !existing.getId().equals(donorId))
                .ifPresent(existing -> { throw new UniqueConflictException("A donor with this phone number already exists."); });
    }

    public static boolean isEligible(LocalDate lastDonationDate) {
        return lastDonationDate == null || !lastDonationDate.plusDays(COOLDOWN_DAYS).isAfter(LocalDate.now());
    }

    private DonorResponse toResponse(Donor donor) {
        boolean eligible = isEligible(donor.getLastDonationDate());
        donor.setAvailable(eligible);
        return new DonorResponse(donor.getId(), donor.getName(), donor.getPhone(),
                donor.getBloodGroup().getBloodGroup(), donor.getCity(), donor.getLastDonationDate(),
                eligible, donor.getCreatedAt());
    }
}