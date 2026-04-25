package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.request.BloodRequestDTO;
import com.rakhtsaathi.dto.response.ApiResponse;
import com.rakhtsaathi.dto.response.BloodRequestResponse;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.service.AuthService;
import com.rakhtsaathi.service.BloodRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;
    private final AuthService authService;

    // POST /api/requests - Create blood request (NEEDY only)
    @PostMapping
    public ResponseEntity<ApiResponse<BloodRequestResponse>> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BloodRequestDTO dto) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        BloodRequestResponse response = bloodRequestService.createRequest(user, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blood request created successfully", response));
    }

    // GET /api/requests/{id} - Get request by ID (any authenticated user)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> getRequest(@PathVariable Long id) {
        BloodRequestResponse response = bloodRequestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success("Request fetched", response));
    }

    // GET /api/requests/my - Get my requests (NEEDY dashboard + history)
    // Frontend: getBloodRequests() filtered by needyId
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<BloodRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String status) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        Page<BloodRequestResponse> requests = bloodRequestService.getMyRequests(user, page, size, status);
        return ResponseEntity.ok(ApiResponse.success("Requests fetched", requests));
    }

    // PUT /api/requests/{id}/cancel - Cancel request
    // Frontend: updateBloodRequest(id, {status: 'CANCELLED'})
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        BloodRequestResponse response = bloodRequestService.cancelRequest(id, user);
        return ResponseEntity.ok(ApiResponse.success("Request cancelled", response));
    }

    // PUT /api/requests/{id}/fulfill - Mark as fulfilled
    // Frontend: updateBloodRequest(id, {status: 'FULFILLED'})
    @PutMapping("/{id}/fulfill")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> fulfillRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        BloodRequestResponse response = bloodRequestService.fulfillRequest(id, user);
        return ResponseEntity.ok(ApiResponse.success("Request marked as fulfilled", response));
    }

    // POST /api/requests/{id}/notify - Manual trigger donor notification
    // Frontend: triggerNotificationManually(id)
    @PostMapping("/{id}/notify")
    public ResponseEntity<ApiResponse<Integer>> triggerNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        int notified = bloodRequestService.triggerNotification(id, user);
        return ResponseEntity.ok(ApiResponse.success(
                "Notification triggered. Notified " + notified + " donors.", notified));
    }
}
