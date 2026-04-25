package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.response.*;
import com.rakhtsaathi.entity.*;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.entity.enums.UserType;
import com.rakhtsaathi.exception.ResourceNotFoundException;
import com.rakhtsaathi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final NeedyRepository needyRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final FeedbackRepository feedbackRepository;
    private final DonationRepository donationRepository;
    private final BloodRequestService bloodRequestService;
    private final DonorService donorService;

    // GET /api/admin/dashboard
    // Frontend: analytics.totalDonors, totalRequests, totalDonations, activeDonors, activeRequests
    public AdminDashboardResponse getDashboard() {
        long totalDonors = userRepository.countByUserType(UserType.DONOR);
        long totalNeedy = userRepository.countByUserType(UserType.NEEDY);
        long totalRequests = bloodRequestRepository.count();
        long activeRequests = bloodRequestRepository.countByStatus(RequestStatus.ACTIVE);
        long fulfilledRequests = bloodRequestRepository.countByStatus(RequestStatus.FULFILLED);
        long cancelledRequests = bloodRequestRepository.countByStatus(RequestStatus.CANCELLED);
        long availableDonors = donorRepository.countByIsAvailableTrue();
        long totalFeedback = feedbackRepository.count();
        long totalDonations = donationRepository.count();

        return AdminDashboardResponse.builder()
                .totalDonors(totalDonors)
                .totalNeedy(totalNeedy)
                .totalUsers(totalDonors + totalNeedy)
                .totalRequests(totalRequests)
                .activeRequests(activeRequests)
                .fulfilledRequests(fulfilledRequests)
                .cancelledRequests(cancelledRequests)
                .activeDonors(availableDonors)
                .totalDonations(totalDonations)
                .totalFeedback(totalFeedback)
                .build();
    }

    // GET /api/admin/requests?status=&bloodGroup=&city=&page=&size=
    // Frontend: getAllRequests() with filters
    public Page<BloodRequestResponse> getAllRequests(int page, int size, String status) {
        return bloodRequestService.getAllRequests(page, size, status);
    }

    // PUT /api/admin/requests/{id}/status
    // Frontend: updateRequestStatus(id, newStatus)
    @Transactional
    public BloodRequestResponse updateRequestStatus(Long requestId, String status) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));

        try {
            request.setStatus(RequestStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Valid values: ACTIVE, FULFILLED, CANCELLED, EXPIRED");
        }

        bloodRequestRepository.save(request);
        log.info("Admin updated request {} status to {}", requestId, status);
        return bloodRequestService.toResponse(request);
    }

    // GET /api/admin/donors?city=&bloodGroup=&status=&page=&size=
    // Frontend: getAllDonors() with filters
    public Page<AdminDonorResponse> getAllDonors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return donorRepository.findAll(pageable).map(this::toDonorResponse);
    }

    // PUT /api/admin/donors/{id}/availability
    // Frontend: updateDonorStatus(donorId, !currentStatus)
    @Transactional
    public AdminDonorResponse toggleDonorAvailability(Long donorId) {
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", donorId));

        donor.setIsAvailable(!donor.getIsAvailable());
        donorRepository.save(donor);
        log.info("Admin toggled donor {} availability to {}", donorId, donor.getIsAvailable());
        return toDonorResponse(donor);
    }

    // GET /api/admin/feedback
    // Frontend: getFeedback() - all feedback, filter suspicious ones (rating <= 2)
    public Page<AdminFeedbackResponse> getAllFeedback(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return feedbackRepository.findAll(pageable).map(this::toFeedbackResponse);
    }

    // GET /api/admin/donations/pending
    // Frontend: getCertificates() - pending donation proofs for approval
    public List<DonationResponse> getPendingDonations() {
        return donationRepository.findAll().stream()
                .filter(d -> "PENDING".equals(d.getStatus()))
                .map(this::toDonationResponse)
                .collect(Collectors.toList());
    }

    // PUT /api/admin/donations/{id}/approve
    // Frontend: handleApproveCertificate(certificateId)
    @Transactional
    public DonationResponse approveDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", donationId));

        donation.setStatus("VERIFIED");
        donationRepository.save(donation);

        // Update donor rating and stats
        Donor donor = donation.getDonor();
        donor.setIsVerified(true);
        donorRepository.save(donor);

        log.info("Admin approved donation {}", donationId);
        return toDonationResponse(donation);
    }

    // PUT /api/admin/donations/{id}/reject
    @Transactional
    public DonationResponse rejectDonation(Long donationId) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Donation", donationId));

        donation.setStatus("REJECTED");
        donationRepository.save(donation);

        log.info("Admin rejected donation {}", donationId);
        return toDonationResponse(donation);
    }

    // GET /api/admin/users
    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable);
    }

    // PUT /api/admin/users/{id}/toggle-status
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        log.info("Admin toggled user {} status to {}", userId, user.getIsActive());
    }

    // Helper: Convert Donor to AdminDonorResponse
    private AdminDonorResponse toDonorResponse(Donor donor) {
        return AdminDonorResponse.builder()
                .id(donor.getId())
                .userId(donor.getUser().getId())
                .fullName(donor.getUser().getFullName())
                .name(donor.getUser().getFullName())
                .email(donor.getUser().getEmail())
                .bloodGroup(donor.getBloodGroup())
                .city(donor.getCity())
                .district(donor.getDistrict())
                .phone(donor.getPhone())
                .contactNumber(donor.getPhone())
                .age(donor.getAge())
                .weight(donor.getWeight())
                .gender(donor.getGender())
                .isAvailable(donor.getIsAvailable())
                .totalDonations(donor.getTotalDonations())
                .donationCount(donor.getDonationCount())
                .rating(donor.getRating())
                .totalFeedbackCount(donor.getTotalFeedbackCount())
                .lastDonationDate(donor.getLastDonationDate())
                .isVerified(donor.getIsVerified())
                .createdAt(donor.getCreatedAt())
                .build();
    }

    // Helper: Convert Feedback to AdminFeedbackResponse
    private AdminFeedbackResponse toFeedbackResponse(Feedback feedback) {
        return AdminFeedbackResponse.builder()
                .id(feedback.getId())
                .requestId(feedback.getBloodRequest() != null ? feedback.getBloodRequest().getId() : null)
                .rating(feedback.getRating())
                .text(feedback.getComment())
                .comment(feedback.getComment())
                .fromUserName(feedback.getFromUser() != null ? feedback.getFromUser().getFullName() : null)
                .toUserName(feedback.getToUser() != null ? feedback.getToUser().getFullName() : null)
                .needyName(feedback.getFromUser() != null ? feedback.getFromUser().getFullName() : null)
                .donorName(feedback.getToUser() != null ? feedback.getToUser().getFullName() : null)
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    // Helper: Convert Donation to DonationResponse
    private DonationResponse toDonationResponse(Donation d) {
        Long reqId = d.getBloodRequest() != null ? d.getBloodRequest().getId() : null;
        return DonationResponse.builder()
                .id(d.getId())
                .donorId(d.getDonor().getId())
                .donorName(d.getDonor().getUser().getFullName())
                .requestId(reqId)
                .requestIdStr(reqId != null ? String.valueOf(reqId) : null)
                .hospitalName(d.getHospitalName())
                .hospital(d.getHospitalName())
                .donationDate(d.getDonationDate())
                .issuedDate(d.getDonationDate())
                .proofImageUrl(d.getProofImageUrl())
                .notes(d.getNotes())
                .units(d.getUnits())
                .status(d.getStatus())
                .certificateId(d.getCertificateId())
                .certificateNumber(d.getCertificateId())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
