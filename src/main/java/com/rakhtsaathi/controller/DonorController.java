package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.request.DonorProfileRequest;
import com.rakhtsaathi.dto.request.DonorRespondRequest;
import com.rakhtsaathi.dto.request.RecordDonationRequest;
import com.rakhtsaathi.dto.response.ApiResponse;
import com.rakhtsaathi.dto.response.BloodRequestResponse;
import com.rakhtsaathi.dto.response.DonationResponse;
import com.rakhtsaathi.dto.response.DonorProfileResponse;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.service.AuthService;
import com.rakhtsaathi.service.BloodRequestService;
import com.rakhtsaathi.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donor")
@RequiredArgsConstructor
@Slf4j
public class DonorController {

    private final DonorService donorService;
    private final BloodRequestService bloodRequestService;
    private final AuthService authService;

    // POST /api/donor/profile - Create donor profile after registration
    // Frontend: createDonor(data) after register
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DonorProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.createProfile(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Donor profile created successfully", response));
    }

    // GET /api/donor/me - Get my profile (replaces getDonorByFirebaseUid)
    // Frontend: getDonorByFirebaseUid(user.uid)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    // GET /api/donor/profile - alias for /me
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    // PUT /api/donor/profile - Update profile
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DonorProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    // PUT /api/donor/availability - Toggle availability
    @PutMapping("/availability")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> toggleAvailability(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.toggleAvailability(user);
        return ResponseEntity.ok(ApiResponse.success(
                "Availability updated to: " + response.getIsAvailable(), response));
    }

    // GET /api/donor/requests - Get blood requests for this donor (city + compatible blood group)
    // Frontend: getBloodRequests({city, status, limit})
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getAvailableRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        List<BloodRequestResponse> requests = donorService.getAvailableRequests(user, bloodRequestService);
        return ResponseEntity.ok(ApiResponse.success("Requests fetched", requests));
    }

    // GET /api/donor/notifications - Get requests specifically notified to this donor
    // Frontend: notifiedRequests (requests where donor was specifically notified)
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<BloodRequestResponse>>> getNotifiedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        List<BloodRequestResponse> requests = donorService.getNotifiedRequests(user, bloodRequestService);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", requests));
    }

    // PUT /api/donor/requests/{id}/accept - Accept a blood request
    // Frontend: respondToBloodRequest(id, donorId, 'ACCEPTED', donorInfo)
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        donorService.acceptRequest(user, requestId);
        return ResponseEntity.ok(ApiResponse.success("Request accepted successfully"));
    }

    // PUT /api/donor/requests/{id}/reject - Reject a blood request
    // Frontend: respondToBloodRequest(id, donorId, 'REJECTED')
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        donorService.rejectRequest(user, requestId);
        return ResponseEntity.ok(ApiResponse.success("Request rejected"));
    }

    // PUT /api/donor/requests/{id}/respond - Generic respond (ACCEPTED or REJECTED)
    // Frontend: respondToBloodRequest(id, donorId, response, donorInfo)
    @PutMapping("/requests/{requestId}/respond")
    public ResponseEntity<ApiResponse<Void>> respondToRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId,
            @Valid @RequestBody DonorRespondRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        if ("ACCEPTED".equalsIgnoreCase(request.getResponse())) {
            donorService.acceptRequest(user, requestId);
            return ResponseEntity.ok(ApiResponse.success("Request accepted successfully"));
        } else {
            donorService.rejectRequest(user, requestId);
            return ResponseEntity.ok(ApiResponse.success("Request rejected"));
        }
    }

    // POST /api/donor/donations - Record donation with proof
    @PostMapping("/donations")
    public ResponseEntity<ApiResponse<DonationResponse>> recordDonation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RecordDonationRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonationResponse donation = donorService.recordDonation(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Donation recorded successfully", donation));
    }

    // GET /api/donor/history - Get donation history
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getDonationHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        List<DonationResponse> history = donorService.getDonationHistory(user);
        return ResponseEntity.ok(ApiResponse.success("Donation history fetched", history));
    }

    // GET /api/donor/certificate/{id} - Get certificate
    @GetMapping("/certificate/{certificateId}")
    public ResponseEntity<ApiResponse<DonationResponse>> getCertificate(
            @PathVariable String certificateId) {

        DonationResponse donation = donorService.getCertificate(certificateId);
        return ResponseEntity.ok(ApiResponse.success("Certificate fetched", donation));
    }

    // GET /api/donor/eligible - Check if donor is eligible to donate
    @GetMapping("/eligible")
    public ResponseEntity<ApiResponse<Boolean>> checkEligibility(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        boolean eligible = donorService.isEligible(user);
        return ResponseEntity.ok(ApiResponse.success(
                eligible ? "You are eligible to donate" : "90-day cooldown period not complete",
                eligible));
    }
}
