package com.rakhtsaathi.repository;

import com.rakhtsaathi.entity.Donation;
import com.rakhtsaathi.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorOrderByCreatedAtDesc(Donor donor);
    Optional<Donation> findByCertificateId(String certificateId);
    long countByDonorAndStatus(Donor donor, String status);
}
