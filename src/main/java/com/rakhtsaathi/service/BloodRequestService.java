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
import java.util.Map;
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

    // Frontend calls: getBloodRequests() - needy dashboard & history
    public Page<BloodRequestResponse> getMyRequests(User user, int page, int size, String status) {
        Needy needy = needyService.getProfile(user);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<BloodRequest> requests;
        if (status != null && !status.equals("ALL")) {
            // Frontend uses 'COMPLETED', backend uses 'FULFILLED' - handle both
            String mappedStatus = status.equals("COMPLETED") ? "FULFILLED" : status;
            try {
                RequestStatus requestStatus = RequestStatus.valueOf(mappedStatus);
                requests = bloodRequestRepository.findByNeedyAndStatusOrderByCreatedAtDesc(needy, requestStatus, pageable);
            } catch (IllegalArgumentException e) {
                requests = bloodRequestRepository.findByNeedyOrderByCreatedAtDesc(needy, pageable);
            }
        } else {
            requests = bloodRequestRepository.findByNeedyOrderByCreatedAtDesc(needy, pageable);
        }

        return requests.map(this::toResponse);
    }

    // Frontend: updateBloodRequest(id, {status: 'CANCELLED'})
    @Transactional
    public BloodRequestResponse cancelRequest(Long requestId, User user) {
        BloodRequest request = getAndValidateOwnership(requestId, user);

        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE requests can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        log.info("Request {} cancelled by user {}", requestId, user.getEmail());
        return toResponse(bloodRequestRepository.save(request));
    }

    // Frontend: updateBloodRequest(id, {status: 'FULFILLED'})
    @Transactional
    public BloodRequestResponse fulfillRequest(Long requestId, User user) {
        BloodRequest request = getAndValidateOwnership(requestId, user);

        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new IllegalArgumentException("Only ACTIVE requests can be fulfilled");
        }

        request.setStatus(RequestStatus.FULFILLED);
        request.setFulfilledAt(LocalDateTime.now());
        log.info("Request {} fulfilled by user {}", requestId, user.getEmail());
        return toResponse(bloodRequestRepository.save(request));
    }

    // Admin: get all requests with pagination + optional status filter
    public Page<BloodRequestResponse> getAllRequests(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (status != null && !status.equals("ALL")) {
            try {
                RequestStatus requestStatus = RequestStatus.valueOf(status);
                return bloodRequestRepository.findByStatusOrderByCreatedAtDesc(requestStatus, pageable)
                        .map(this::toResponse);
            } catch (IllegalArgumentException e) {
                // fall through to all
            }
        }
        return bloodRequestRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    private BloodRequest getAndValidateOwnership(Long requestId, User user) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        if (!request.getNeedy().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only modify your own requests");
        }
        return request;
    }

    @Transactional
    public int triggerNotification(Long requestId, User user) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        // Validate ownership
        if (!request.getNeedy().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only trigger notifications for your own requests");
        }

        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only trigger notifications for ACTIVE requests");
        }

        // Trigger async notification
        notificationService.notifyDonors(request);
        log.info("Manual notification triggered for request {} by user {}", requestId, user.getEmail());
        return request.getNotifiedDonorsCount();
    }

    public BloodRequestResponse toResponse(BloodRequest request) {
        List<DonorNotificationResponse> notificationList = notificationRepository
                .findByBloodRequest(request)
                .stream()
                .map(n -> DonorNotificationResponse.builder()
                        .donorId(n.getDonor().getId())
                        .donorName(n.getDonor().getUser().getFullName())
                        .bloodGroup(n.getDonor().getBloodGroup())
                        .city(n.getDonor().getCity())
                        .phone(n.getStatus() == com.rakhtsaathi.entity.enums.DonorResponseStatus.ACCEPTED
                                ? n.getDonor().getPhone() : null)
                        .status(n.getStatus())
                        .notifiedAt(n.getNotifiedAt())
                        .respondedAt(n.getRespondedAt())
                        .build())
                .collect(Collectors.toList());

        // Build notifiedDonors map matching frontend's expected format
        // Frontend uses: request.notifiedDonors[donorId].donorInfo.fullName etc.
        Map<String, BloodRequestResponse.DonorNotificationMapEntry> notifiedDonorsMap =
                notificationRepository.findByBloodRequest(request)
                        .stream()
                        .collect(Collectors.toMap(
                                n -> String.valueOf(n.getDonor().getId()),
                                n -> BloodRequestResponse.DonorNotificationMapEntry.builder()
                                        .status(n.getStatus().name())
                                        .notifiedAt(n.getNotifiedAt())
                                        .respondedAt(n.getRespondedAt())
                                        .donorInfo(BloodRequestResponse.DonorNotificationMapEntry.DonorInfo.builder()
                                                .fullName(n.getDonor().getUser().getFullName())
                                                .bloodGroup(n.getDonor().getBloodGroup().name())
                                                .city(n.getDonor().getCity())
                                                // Only expose phone if donor accepted
                                                .phone(n.getStatus() == com.rakhtsaathi.entity.enums.DonorResponseStatus.ACCEPTED
                                                        ? n.getDonor().getPhone() : null)
                                                .build())
                                        .build(),
                                (a, b) -> a // keep first on duplicate key
                        ));

        return BloodRequestResponse.builder()
                .id(request.getId())
                .idStr(String.valueOf(request.getId()))  // for frontend .slice(-6)
                .patientName(request.getPatientName())
                .bloodGroup(request.getBloodGroup())
                .unitsNeeded(request.getUnitsNeeded())
                .unitsRequired(request.getUnitsNeeded())   // alias for frontend
                .urgency(request.getUrgency())
                .urgencyLevel(request.getUrgency())        // alias for frontend
                .hospital(request.getHospital())
                .hospitalName(request.getHospital())       // alias for frontend
                .city(request.getCity())
                .attendantName(request.getAttendantName())
                .contactNumber(request.getContactNumber())
                .attendantPhone(request.getContactNumber())
                .additionalNotes(request.getAdditionalNotes())
                .voiceMessageUrl(request.getVoiceMessageUrl())
                .hasVoiceMessage(request.getHasVoiceMessage())
                .status(request.getStatus())
                .notifiedDonorsCount(request.getNotifiedDonorsCount())
                .acceptedDonorsCount(request.getAcceptedDonorsCount())
                .rejectedDonorsCount(request.getRejectedDonorsCount())
                .donorNotifications(notificationList)
                .notifiedDonors(notifiedDonorsMap)
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .fulfilledAt(request.getFulfilledAt())
                .build();
    }
}
