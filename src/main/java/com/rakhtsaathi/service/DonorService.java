package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.request.DonorProfileRequest;
import com.rakhtsaathi.dto.request.RecordDonationRequest;
import com.rakhtsaathi.dto.response.BloodRequestResponse;
import com.rakhtsaathi.dto.response.DonationResponse;
import com.rakhtsaathi.dto.response.DonorProfileResponse;
import com.rakhtsaathi.entity.*;
import com.rakhtsaathi.entity.enums.BloodGroup;
import com.rakhtsaathi.entity.enums.DonorResponseStatus;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.exception.ResourceNotFoundException;
import com.rakhtsaathi.exception.UnauthorizedException;
import com.rakhtsaathi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorService {

    private final DonorRepository donorRepository;
    private final DonorNotificationRepository notificationRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonationRepository donationRepository;
    private final NotificationService notificationService;

    // Blood group compatibility - donor can donate TO these groups
    private static final Map<BloodGroup, List<BloodGroup>> CAN_DONATE_TO = new HashMap<>();
    static {
        CAN_DONATE_TO.put(BloodGroup.O_NEGATIVE, Arrays.asList(BloodGroup.values())); // Universal donor
        CAN_DONATE_TO.put(BloodGroup.O_POSITIVE, List.of(BloodGroup.O_POSITIVE, BloodGroup.A_POSITIVE, BloodGroup.B_POSITIVE, BloodGroup.AB_POSITIVE));
        CAN_DONATE_TO.put(BloodGroup.A_NEGATIVE, List.of(BloodGroup.A_NEGATIVE, BloodGroup.A_POSITIVE, BloodGroup.AB_NEGATIVE, BloodGroup.AB_POSITIVE));
        CAN_DONATE_TO.put(BloodGroup.A_POSITIVE, List.of(BloodGroup.A_POSITIVE, BloodGroup.AB_POSITIVE));
        CAN_DONATE_TO.put(BloodGroup.B_NEGATIVE, List.of(BloodGroup.B_NEGATIVE, BloodGroup.B_POSITIVE, BloodGroup.AB_NEGATIVE, BloodGroup.AB_POSITIVE));
        CAN_DONATE_TO.put(BloodGroup.B_POSITIVE, List.of(BloodGroup.B_POSITIVE, BloodGroup.AB_POSITIVE));
        CAN_DONATE_TO.put(BloodGroup.AB_NEGATIVE, List.of(BloodGroup.AB_NEGATIVE, BloodGroup.AB_POSITIVE));
        CAN_DONATE_TO.put(BloodGroup.AB_POSITIVE, List.of(BloodGroup.AB_POSITIVE));
    }

    @Transactional
    public DonorProfileResponse createProfile(User user, DonorProfileRequest request) {
        log.info("Creating donor profile for user: {}", user.getEmail());

        if (donorRepository.findByUser(user).isPresent()) {
            throw new IllegalArgumentException("Donor profile already exists for this user");
        }

        LocalDate lastDonation = null;
        if (request.getLastDonationDate() != null && !request.getLastDonationDate().isEmpty()) {
            try {
                lastDonation = LocalDate.parse(request.getLastDonationDate());
            } catch (Exception e) {
                log.warn("Could not parse lastDonationDate: {}", request.getLastDonationDate());
            }
        }

        Donor donor = Donor.builder()
                .user(user)
                .bloodGroup(request.getBloodGroup())
                .city(request.getCity())
                .district(request.getDistrict())
                .phone(request.getPhone())
                .age(request.getAge())
                .weight(request.getWeight())
                .gender(request.getGender())
                .aadhaarNumber(request.getAadhaarNumber())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .totalDonations(0)
                .donationCount(0)
                .rating(0.0)
                .totalFeedbackCount(0)
                .lastDonationDate(lastDonation)
                .isVerified(false)
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
        if (request.getDistrict() != null) donor.setDistrict(request.getDistrict());
        donor.setPhone(request.getPhone());
        donor.setAge(request.getAge());
        donor.setWeight(request.getWeight());
        if (request.getGender() != null) donor.setGender(request.getGender());
        if (request.getIsAvailable() != null) donor.setIsAvailable(request.getIsAvailable());

        if (request.getLastDonationDate() != null && !request.getLastDonationDate().isEmpty()) {
            try {
                donor.setLastDonationDate(LocalDate.parse(request.getLastDonationDate()));
            } catch (Exception e) {
                log.warn("Could not parse lastDonationDate: {}", request.getLastDonationDate());
            }
        }

        return toResponse(donorRepository.save(donor));
    }

    public DonorProfileResponse getProfile(User user) {
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found. Please complete registration."));
        return toResponse(donor);
    }

    public Donor getDonorEntity(User user) {
        return donorRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found."));
    }

    // Toggle availability
    @Transactional
    public DonorProfileResponse toggleAvailability(User user) {
        Donor donor = getDonorEntity(user);
        donor.setIsAvailable(!donor.getIsAvailable());
        return toResponse(donorRepository.save(donor));
    }

    // Get blood requests for this donor (by city + compatible blood group)
    public List<BloodRequestResponse> getAvailableRequests(User user, BloodRequestService bloodRequestService) {
        Donor donor = getDonorEntity(user);

        // Get active requests in donor's city
        List<BloodRequest> cityRequests = bloodRequestRepository.findActiveRequestsByCity(donor.getCity());

        // Filter by blood group compatibility
        List<BloodGroup> canDonateTo = CAN_DONATE_TO.getOrDefault(donor.getBloodGroup(), List.of());

        return cityRequests.stream()
                .filter(req -> canDonateTo.contains(req.getBloodGroup()))
                .map(bloodRequestService::toResponse)
                .collect(Collectors.toList());
    }

    // Get requests specifically notified to this donor
    public List<BloodRequestResponse> getNotifiedRequests(User user, BloodRequestService bloodRequestService) {
        Donor donor = getDonorEntity(user);

        List<DonorNotification> notifications = notificationRepository.findByDonor(donor);

        return notifications.stream()
                .filter(n -> n.getBloodRequest().getStatus() == RequestStatus.ACTIVE)
                .map(n -> bloodRequestService.toResponse(n.getBloodRequest()))
                .collect(Collectors.toList());
    }

    // Accept a blood request
    @Transactional
    public void acceptRequest(User user, Long requestId) {
        Donor donor = getDonorEntity(user);
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        DonorNotification notification = notificationRepository
                .findByBloodRequestAndDonor(bloodRequest, donor)
                .orElseGet(() -> {
                    // Create notification if not exists (donor volunteering)
                    DonorNotification n = DonorNotification.builder()
                            .bloodRequest(bloodRequest)
                            .donor(donor)
                            .status(DonorResponseStatus.NOTIFIED)
                            .notifiedAt(LocalDateTime.now())
                            .build();
                    return notificationRepository.save(n);
                });

        if (notification.getStatus() == DonorResponseStatus.ACCEPTED) {
            throw new IllegalArgumentException("You have already accepted this request");
        }

        notification.setStatus(DonorResponseStatus.ACCEPTED);
        notification.setRespondedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        bloodRequest.setAcceptedDonorsCount(bloodRequest.getAcceptedDonorsCount() + 1);
        bloodRequestRepository.save(bloodRequest);

        // Mark donor as unavailable
        donor.setIsAvailable(false);
        donorRepository.save(donor);

        log.info("Donor {} accepted request {}", donor.getId(), requestId);
    }

    // Reject a blood request
    @Transactional
    public void rejectRequest(User user, Long requestId) {
        Donor donor = getDonorEntity(user);
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        DonorNotification notification = notificationRepository
                .findByBloodRequestAndDonor(bloodRequest, donor)
                .orElseThrow(() -> new UnauthorizedException("You were not notified for this request"));

        if (notification.getStatus() == DonorResponseStatus.REJECTED) {
            throw new IllegalArgumentException("You have already rejected this request");
        }

        notification.setStatus(DonorResponseStatus.REJECTED);
        notification.setRespondedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        bloodRequest.setRejectedDonorsCount(bloodRequest.getRejectedDonorsCount() + 1);
        bloodRequestRepository.save(bloodRequest);

        log.info("Donor {} rejected request {}", donor.getId(), requestId);
    }

    // Record donation with proof
    @Transactional
    public DonationResponse recordDonation(User user, RecordDonationRequest request) {
        Donor donor = getDonorEntity(user);

        BloodRequest bloodRequest = null;
        if (request.getRequestId() != null) {
            bloodRequest = bloodRequestRepository.findById(request.getRequestId()).orElse(null);
        }

        LocalDate donationDate;
        try {
            donationDate = LocalDate.parse(request.getDonationDate());
        } catch (Exception e) {
            donationDate = LocalDate.now();
        }

        String certificateId = "BS-" + LocalDate.now().getYear() + "-" +
                String.format("%06d", (long)(Math.random() * 1000000));

        Donation donation = Donation.builder()
                .donor(donor)
                .bloodRequest(bloodRequest)
                .hospitalName(request.getHospitalName())
                .donationDate(donationDate)
                .proofImageUrl(request.getImageUrl())
                .notes(request.getNotes())
                .units(request.getUnits() != null ? request.getUnits() : 1)
                .status("PENDING")
                .certificateId(certificateId)
                .build();

        Donation saved = donationRepository.save(donation);

        // Update donor stats
        donor.setLastDonationDate(donationDate);
        donor.setTotalDonations(donor.getTotalDonations() + 1);
        donor.setDonationCount(donor.getDonationCount() + 1);
        donorRepository.save(donor);

        log.info("Donation recorded for donor {} with certificate {}", donor.getId(), certificateId);
        return toDonationResponse(saved);
    }

    // Get donation history
    public List<DonationResponse> getDonationHistory(User user) {
        Donor donor = getDonorEntity(user);
        return donationRepository.findByDonorOrderByCreatedAtDesc(donor)
                .stream().map(this::toDonationResponse).collect(Collectors.toList());
    }

    // Get certificate by ID
    public DonationResponse getCertificate(String certificateId) {
        Donation donation = donationRepository.findByCertificateId(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));
        return toDonationResponse(donation);
    }

    private DonationResponse toDonationResponse(Donation d) {
        Long reqId = d.getBloodRequest() != null ? d.getBloodRequest().getId() : null;
        return DonationResponse.builder()
                .id(d.getId())
                .donorId(d.getDonor().getId())
                .donorName(d.getDonor().getUser().getFullName())
                .requestId(reqId)
                .requestIdStr(reqId != null ? String.valueOf(reqId) : null)
                .hospitalName(d.getHospitalName())
                .hospital(d.getHospitalName())              // alias for certificate page
                .donationDate(d.getDonationDate())
                .issuedDate(d.getDonationDate())            // alias for certificate page
                .proofImageUrl(d.getProofImageUrl())
                .notes(d.getNotes())
                .units(d.getUnits())
                .status(d.getStatus())
                .certificateId(d.getCertificateId())
                .certificateNumber(d.getCertificateId())    // alias for certificate page
                .createdAt(d.getCreatedAt())
                .build();
    }

    // Check eligibility (90 days since last donation)
    public boolean isEligible(User user) {
        Donor donor = getDonorEntity(user);
        if (donor.getLastDonationDate() == null) return true;
        return donor.getLastDonationDate().plusDays(90).isBefore(LocalDate.now());
    }

    public DonorProfileResponse toResponse(Donor donor) {
        String maskedAadhaar = donor.getAadhaarNumber() != null ?
                "****-****-" + donor.getAadhaarNumber().substring(Math.max(0, donor.getAadhaarNumber().length() - 4)) : null;

        return DonorProfileResponse.builder()
                .id(donor.getId())
                .userId(donor.getUser().getId())
                .fullName(donor.getUser().getFullName())
                .name(donor.getUser().getFullName())          // alias for frontend
                .email(donor.getUser().getEmail())
                .bloodGroup(donor.getBloodGroup())
                .city(donor.getCity())
                .district(donor.getDistrict())
                .phone(donor.getPhone())
                .contactNumber(donor.getPhone())              // alias for frontend
                .age(donor.getAge())
                .weight(donor.getWeight())
                .gender(donor.getGender())
                .aadhaarNumber(maskedAadhaar)
                .isAvailable(donor.getIsAvailable())
                .totalDonations(donor.getTotalDonations())
                .donationCount(donor.getDonationCount())
                .rating(donor.getRating())
                .totalFeedbackCount(donor.getTotalFeedbackCount())
                .lastDonationDate(donor.getLastDonationDate())
                .isVerified(donor.getIsVerified())
                .createdAt(donor.getCreatedAt())
                .updatedAt(donor.getUpdatedAt())
                .build();
    }
}
