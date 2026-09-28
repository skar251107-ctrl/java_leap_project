package com.example.demo.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.DonationRequest;
import com.example.demo.dto.DonationResponse;
import com.example.demo.entity.DonationRecord;
import com.example.demo.entity.Donor;
import com.example.demo.exception.CooldownException;
import com.example.demo.exception.DonationNotFoundException;
import com.example.demo.repository.DonationRecordRepository;
import com.example.demo.repository.DonorRepository;

@Service
public class DonationService {
    private static final long COOLDOWN_DAYS = 90;
    private final DonationRecordRepository donationRepository;
    private final DonorService donorService;
    private final DonorRepository donorRepository;

    public DonationService(DonationRecordRepository donationRepository, DonorService donorService,
            DonorRepository donorRepository) {
        this.donationRepository = donationRepository;
        this.donorService = donorService;
        this.donorRepository = donorRepository;
    }

    @Transactional
    public DonationResponse create(DonationRequest request) {
        Donor donor = donorService.findById(request.donorId());
        LocalDate latest = latestDonationDate(donor, null);
        if (latest != null && request.donationDate().isBefore(latest.plusDays(COOLDOWN_DAYS))) {
            throw new CooldownException("Donor must wait 90 days between donations. Eligible on "
                    + latest.plusDays(COOLDOWN_DAYS) + ".");
        }
        DonationRecord donation = new DonationRecord();
        donation.setDonor(donor);
        donation.setDonationDate(request.donationDate());
        donation.setHospitalName(request.hospitalName().trim());
        DonationRecord saved = donationRepository.save(donation);
        syncDonor(donor);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> getAll() {
        return donationRepository.findAll().stream()
            .sorted(Comparator.comparing((DonationRecord donation) -> donation.getDonationDate()).reversed())
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DonationResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public DonationResponse update(Long id, DonationRequest request) {
        DonationRecord donation = findById(id);
        Donor previousDonor = donation.getDonor();
        Donor donor = donorService.findById(request.donorId());
        LocalDate latestOther = latestDonationDate(donor,
                previousDonor.getId().equals(donor.getId()) ? id : null);
        if (latestOther != null && request.donationDate().isBefore(latestOther.plusDays(COOLDOWN_DAYS))) {
            throw new CooldownException("Donor must wait 90 days between donations. Eligible on "
                    + latestOther.plusDays(COOLDOWN_DAYS) + ".");
        }
        donation.setDonor(donor);
        donation.setDonationDate(request.donationDate());
        donation.setHospitalName(request.hospitalName().trim());
        DonationRecord saved = donationRepository.save(donation);
        syncDonor(previousDonor);
        if (!previousDonor.getId().equals(donor.getId())) {
            syncDonor(donor);
        }
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        DonationRecord donation = findById(id);
        Donor donor = donation.getDonor();
        donationRepository.delete(donation);
        syncDonor(donor);
    }

    @Transactional(readOnly = true)
    public List<DonationResponse> getForDonor(Long donorId) {
        donorService.findById(donorId);
        return donationRepository.findByDonor_IdOrderByDonationDateDesc(donorId).stream()
                .map(this::toResponse).toList();
    }

    private DonationRecord findById(Long id) {
        return donationRepository.findById(id).orElseThrow(() -> new DonationNotFoundException(id));
    }

    private LocalDate latestDonationDate(Donor donor, Long excludingId) {
        LocalDate fromRecords = donationRepository.findByDonor_IdOrderByDonationDateDesc(donor.getId()).stream()
                .filter(record -> excludingId == null || !record.getId().equals(excludingId))
            .map(record -> record.getDonationDate()).max(Comparator.naturalOrder()).orElse(null);
        LocalDate recordedOnDonor = donor.getLastDonationDate();
        if (excludingId != null && recordedOnDonor != null) {
            LocalDate donorDate = recordedOnDonor;
            boolean representedByExcludedRecord = donationRepository.findById(excludingId)
                .map(record -> record.getDonationDate().equals(donorDate)).orElse(false);
            if (representedByExcludedRecord) {
                recordedOnDonor = null;
            }
        }
        return fromRecords == null ? recordedOnDonor
                : recordedOnDonor == null ? fromRecords : fromRecords.isAfter(recordedOnDonor) ? fromRecords : recordedOnDonor;
    }

    private void syncDonor(Donor donor) {
        LocalDate latest = donationRepository.findByDonor_IdOrderByDonationDateDesc(donor.getId()).stream()
            .map(record -> record.getDonationDate()).max(Comparator.naturalOrder()).orElse(null);
        donor.setLastDonationDate(latest);
        donor.setAvailable(DonorService.isEligible(latest));
        donorRepository.save(donor);
    }

    private DonationResponse toResponse(DonationRecord donation) {
        return new DonationResponse(donation.getId(), donation.getDonor().getId(), donation.getDonor().getName(),
                donation.getDonationDate(), donation.getHospitalName(), donation.getCreatedAt());
    }
}