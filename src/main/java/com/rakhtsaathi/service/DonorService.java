package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.request.DonorProfileRequest;
import com.rakhtsaathi.dto.response.BloodRequestResponse;
import com.rakhtsaathi.dto.response.DonorProfileResponse;
import com.rakhtsaathi.entity.*;
import com.rakhtsaathi.entity.enums.DonorResponseStatus;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.exception.ResourceNotFoundException;
import com.rakhtsaathi.exception.UnauthorizedException;
import com.rakhtsaathi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorService {

    private final DonorRepository donorRepository;
    private final DonorNotificationRepository notificationRepository;
    private final BloodRequestRepository bloodRequestRepository;

    @Transactional
    public DonorProfileResponse createProfile(User user, DonorProfileRequest request) {
        log.info("Creating donor profile for user: {}", user.getEmail());

        if (donorRepository.findByUser(user).isPresent()) {
            throw new IllegalArgumentException("Donor profile already exists for this user");
        }

        Donor donor = Donor.builder()
                .user(user)
                .bloodGroup(request.getBloodGroup())
                .city(request.getCity())
                .phone(request.getPhone())
                .age(request.getAge())
                .weight(request.getWeight())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .totalDonations(0)
                .rating(0.0)
                .build();

        Donor saved = donorRepository.save(donor);
        log.info("Donor profile created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public DonorProfileResponse updateProfile(User user, DonorProfileRequest request) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        donor.setBloodGroup(request.getBloodGroup());
        donor.setCity(request.getCity());
        donor.setPhone(request.getPhone());
        donor.setAge(request.getAge());
        donor.setWeight(request.getWeight());
        if (request.getIsAvailable() != null) {
            donor.setIsAvailable(request.getIsAvailable());
        }

        return toResponse(donorRepository.save(donor));
    }

    public DonorProfileResponse getProfile(User user) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found. Please complete registration."));
        return toResponse(donor);
    }

    // Get blood requests assigned to this donor
    public Page<DonorNotification> getMyNotifications(User user, int page, int size) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<DonorNotification> notifications = notificationRepository.findByDonor(donor);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), notifications.size());
        List<DonorNotification> pageContent = notifications.subList(start, end);

        return new PageImpl<>(pageContent, pageable, notifications.size());
    }

    // Accept a blood request
    @Transactional
    public void acceptRequest(User user, Long requestId) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        DonorNotification notification = notificationRepository
                .findByBloodRequestAndDonor(bloodRequest, donor)
                .orElseThrow(() -> new UnauthorizedException("You were not notified for this request"));

        if (notification.getStatus() != DonorResponseStatus.NOTIFIED) {
            throw new IllegalArgumentException("You have already responded to this request");
        }

        notification.setStatus(DonorResponseStatus.ACCEPTED);
        notification.setRespondedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // Update accepted count on request
        bloodRequest.setAcceptedDonorsCount(bloodRequest.getAcceptedDonorsCount() + 1);
        bloodRequestRepository.save(bloodRequest);

        // Mark donor as unavailable temporarily
        donor.setIsAvailable(false);
        donorRepository.save(donor);

        log.info("Donor {} accepted request {}", donor.getId(), requestId);
    }

    // Reject a blood request
    @Transactional
    public void rejectRequest(User user, Long requestId) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        DonorNotification notification = notificationRepository
                .findByBloodRequestAndDonor(bloodRequest, donor)
                .orElseThrow(() -> new UnauthorizedException("You were not notified for this request"));

        if (notification.getStatus() != DonorResponseStatus.NOTIFIED) {
            throw new IllegalArgumentException("You have already responded to this request");
        }

        notification.setStatus(DonorResponseStatus.REJECTED);
        notification.setRespondedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // Update rejected count
        bloodRequest.setRejectedDonorsCount(bloodRequest.getRejectedDonorsCount() + 1);
        bloodRequestRepository.save(bloodRequest);

        log.info("Donor {} rejected request {}", donor.getId(), requestId);
    }

    // Mark donation as complete
    @Transactional
    public void completeDonation(User user, Long requestId) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        donor.setTotalDonations(donor.getTotalDonations() + 1);
        donor.setLastDonationDate(LocalDate.now());
        donor.setIsAvailable(false); // 90 days cooldown
        donorRepository.save(donor);

        log.info("Donor {} completed donation for request {}", donor.getId(), requestId);
    }

    // Get donation history
    public List<DonorNotification> getDonationHistory(User user) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        return notificationRepository.findByDonorAndStatus(donor, DonorResponseStatus.ACCEPTED);
    }

    private DonorProfileResponse toResponse(Donor donor) {
        return DonorProfileResponse.builder()
                .id(donor.getId())
                .userId(donor.getUser().getId())
                .fullName(donor.getUser().getFullName())
                .email(donor.getUser().getEmail())
                .bloodGroup(donor.getBloodGroup())
                .city(donor.getCity())
                .phone(donor.getPhone())
                .age(donor.getAge())
                .weight(donor.getWeight())
                .isAvailable(donor.getIsAvailable())
                .totalDonations(donor.getTotalDonations())
                .rating(donor.getRating())
                .lastDonationDate(donor.getLastDonationDate())
                .createdAt(donor.getCreatedAt())
                .build();
    }
}
