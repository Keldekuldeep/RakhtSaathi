package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.response.AdminDashboardResponse;
import com.rakhtsaathi.dto.response.BloodRequestResponse;
import com.rakhtsaathi.dto.response.DonorProfileResponse;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final DonorRepository donorRepository;
    private final NeedyRepository needyRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final FeedbackRepository feedbackRepository;
    private final BloodRequestService bloodRequestService;

    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalDonors(userRepository.countByUserType(UserType.DONOR))
                .totalNeedy(userRepository.countByUserType(UserType.NEEDY))
                .totalRequests(bloodRequestRepository.count())
                .activeRequests(bloodRequestRepository.countByStatus(RequestStatus.ACTIVE))
                .fulfilledRequests(bloodRequestRepository.countByStatus(RequestStatus.FULFILLED))
                .cancelledRequests(bloodRequestRepository.countByStatus(RequestStatus.CANCELLED))
                .availableDonors(donorRepository.countByIsAvailableTrue())
                .totalFeedback(feedbackRepository.count())
                .build();
    }

    public Page<BloodRequestResponse> getAllRequests(int page, int size) {
        return bloodRequestService.getAllRequests(page, size);
    }

    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable);
    }

    public Page<Donor> getAllDonors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return donorRepository.findAll(pageable);
    }

    public Page<Needy> getAllNeedy(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return needyRepository.findAll(pageable);
    }

    public Page<Feedback> getAllFeedback(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return feedbackRepository.findAll(pageable);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        log.info("User {} status toggled to: {}", userId, user.getIsActive());
    }

    @Transactional
    public BloodRequestResponse updateRequestStatus(Long requestId, RequestStatus status) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", requestId));
        request.setStatus(status);
        bloodRequestRepository.save(request);
        log.info("Admin updated request {} status to {}", requestId, status);
        return bloodRequestService.getRequestById(requestId);
    }
}
