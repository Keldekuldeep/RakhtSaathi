package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.request.BloodRequestDTO;
import com.rakhtsaathi.dto.response.BloodRequestResponse;
import com.rakhtsaathi.dto.response.DonorNotificationResponse;
import com.rakhtsaathi.entity.*;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.exception.ResourceNotFoundException;
import com.rakhtsaathi.exception.UnauthorizedException;
import com.rakhtsaathi.repository.BloodRequestRepository;
import com.rakhtsaathi.repository.DonorNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final DonorNotificationRepository notificationRepository;
    private final NeedyService needyService;
    private final NotificationService notificationService;

    @Transactional
    public BloodRequestResponse createRequest(User user, BloodRequestDTO dto) {
        log.info("Creating blood request for user: {}", user.getEmail());

        Needy needy = needyService.getProfile(user);

        BloodRequest request = BloodRequest.builder()
                .needy(needy)
                .patientName(dto.getPatientName())
                .bloodGroup(dto.getBloodGroup())
                .unitsNeeded(dto.getUnitsNeeded())
                .urgency(dto.getUrgency())
                .hospital(dto.getHospital())
                .city(dto.getCity())
                .attendantName(dto.getAttendantName())
                .contactNumber(dto.getContactNumber())
                .additionalNotes(dto.getAdditionalNotes())
                .status(RequestStatus.ACTIVE)
                .notifiedDonorsCount(0)
                .acceptedDonorsCount(0)
                .rejectedDonorsCount(0)
                .build();

        BloodRequest saved = bloodRequestRepository.save(request);

        // Update needy request count
        needy.setRequestCount(needy.getRequestCount() + 1);

        // Trigger async donor notification
        notificationService.notifyDonors(saved);

        log.info("Blood request created with id: {}", saved.getId());
        return toResponse(saved);
    }

    public BloodRequestResponse getRequestById(Long requestId) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));
        return toResponse(request);
    }

    public Page<BloodRequestResponse> getMyRequests(User user, int page, int size) {
        Needy needy = needyService.getProfile(user);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BloodRequest> requests = bloodRequestRepository.findByNeedyOrderByCreatedAtDesc(needy, pageable);
        return requests.map(this::toResponse);
    }

    @Transactional
    public BloodRequestResponse cancelRequest(Long requestId, User user) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        if (!request.getNeedy().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only cancel your own requests");
        }

        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE requests can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        return toResponse(bloodRequestRepository.save(request));
    }

    @Transactional
    public BloodRequestResponse fulfillRequest(Long requestId, User user) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        if (!request.getNeedy().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only fulfill your own requests");
        }

        request.setStatus(RequestStatus.FULFILLED);
        request.setFulfilledAt(LocalDateTime.now());
        return toResponse(bloodRequestRepository.save(request));
    }

    // Admin: get all requests with pagination
    public Page<BloodRequestResponse> getAllRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return bloodRequestRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    private BloodRequestResponse toResponse(BloodRequest request) {
        List<DonorNotificationResponse> notifications = notificationRepository
                .findByBloodRequest(request)
                .stream()
                .map(n -> DonorNotificationResponse.builder()
                        .donorId(n.getDonor().getId())
                        .donorName(n.getDonor().getUser().getFullName())
                        .bloodGroup(n.getDonor().getBloodGroup())
                        .city(n.getDonor().getCity())
                        .phone(n.getStatus() == com.rakhtsaathi.entity.enums.DonorResponseStatus.ACCEPTED
                                ? n.getDonor().getPhone() : null) // Only show phone if accepted
                        .status(n.getStatus())
                        .notifiedAt(n.getNotifiedAt())
                        .respondedAt(n.getRespondedAt())
                        .build())
                .collect(Collectors.toList());

        return BloodRequestResponse.builder()
                .id(request.getId())
                .patientName(request.getPatientName())
                .bloodGroup(request.getBloodGroup())
                .unitsNeeded(request.getUnitsNeeded())
                .urgency(request.getUrgency())
                .hospital(request.getHospital())
                .city(request.getCity())
                .attendantName(request.getAttendantName())
                .contactNumber(request.getContactNumber())
                .additionalNotes(request.getAdditionalNotes())
                .status(request.getStatus())
                .notifiedDonorsCount(request.getNotifiedDonorsCount())
                .acceptedDonorsCount(request.getAcceptedDonorsCount())
                .rejectedDonorsCount(request.getRejectedDonorsCount())
                .donorNotifications(notifications)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
