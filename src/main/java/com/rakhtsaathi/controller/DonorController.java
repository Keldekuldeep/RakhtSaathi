package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.request.DonorProfileRequest;
import com.rakhtsaathi.dto.response.ApiResponse;
import com.rakhtsaathi.dto.response.DonorProfileResponse;
import com.rakhtsaathi.entity.DonorNotification;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.service.AuthService;
import com.rakhtsaathi.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    private final AuthService authService;

    // POST /api/donor/profile
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DonorProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.createProfile(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Donor profile created successfully", response));
    }

    // GET /api/donor/profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    // PUT /api/donor/profile
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DonorProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        DonorProfileResponse response = donorService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    // GET /api/donor/notifications  - blood requests assigned to this donor
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Page<DonorNotification>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        Page<DonorNotification> notifications = donorService.getMyNotifications(user, page, size);
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched", notifications));
    }

    // PUT /api/donor/requests/{requestId}/accept
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        donorService.acceptRequest(user, requestId);
        return ResponseEntity.ok(ApiResponse.success("Request accepted successfully"));
    }

    // PUT /api/donor/requests/{requestId}/reject
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        donorService.rejectRequest(user, requestId);
        return ResponseEntity.ok(ApiResponse.success("Request rejected"));
    }

    // PUT /api/donor/requests/{requestId}/complete
    @PutMapping("/requests/{requestId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeDonation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        donorService.completeDonation(user, requestId);
        return ResponseEntity.ok(ApiResponse.success("Donation marked as complete"));
    }

    // GET /api/donor/history
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<DonorNotification>>> getDonationHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        List<DonorNotification> history = donorService.getDonationHistory(user);
        return ResponseEntity.ok(ApiResponse.success("Donation history fetched", history));
    }
}
