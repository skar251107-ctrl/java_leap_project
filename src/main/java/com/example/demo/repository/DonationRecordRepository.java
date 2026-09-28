package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.DonationRecord;

public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {
    List<DonationRecord> findByDonor_IdOrderByDonationDateDesc(Long donorId);
    Optional<DonationRecord> findFirstByDonor_IdOrderByDonationDateDesc(Long donorId);
}