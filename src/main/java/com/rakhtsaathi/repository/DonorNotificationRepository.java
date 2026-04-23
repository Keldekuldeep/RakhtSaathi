package com.rakhtsaathi.repository;

import com.rakhtsaathi.entity.BloodRequest;
import com.rakhtsaathi.entity.Donor;
import com.rakhtsaathi.entity.DonorNotification;
import com.rakhtsaathi.entity.enums.DonorResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorNotificationRepository extends JpaRepository<DonorNotification, Long> {
    List<DonorNotification> findByBloodRequest(BloodRequest bloodRequest);
    List<DonorNotification> findByDonor(Donor donor);
    Optional<DonorNotification> findByBloodRequestAndDonor(BloodRequest bloodRequest, Donor donor);
    List<DonorNotification> findByBloodRequestAndStatus(BloodRequest bloodRequest, DonorResponseStatus status);
    List<DonorNotification> findByDonorAndStatus(Donor donor, DonorResponseStatus status);
    long countByBloodRequestAndStatus(BloodRequest bloodRequest, DonorResponseStatus status);
}
