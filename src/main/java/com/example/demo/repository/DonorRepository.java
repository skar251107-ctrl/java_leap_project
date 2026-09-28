package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Donor;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    Optional<Donor> findByPhone(String phone);
    boolean existsByBloodGroup_Id(Long bloodGroupId);

    @Query("select d from Donor d where (:bloodGroup is null or d.bloodGroup.bloodGroup = :bloodGroup) "
            + "and (:city is null or lower(d.city) like lower(concat('%', :city, '%'))) "
            + "and (d.lastDonationDate is null or d.lastDonationDate <= :cutoff)")
    List<Donor> searchAvailable(@Param("bloodGroup") String bloodGroup,
            @Param("city") String city, @Param("cutoff") LocalDate cutoff);

    long countByBloodGroup_Id(Long bloodGroupId);
}