package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.response.*;
import com.rakhtsaathi.entity.*;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // GET /api/admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched", dashboard));
    }

    // GET /api/admin/requests
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<BloodRequestResponse>>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BloodRequestResponse> requests = adminService.getAllRequests(page, size);
        return ResponseEntity.ok(ApiResponse.success("Requests fetched", requests));
    }

    // PUT /api/admin/requests/{id}/status
    @PutMapping("/requests/{id}/status")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus status) {

        BloodRequestResponse response = adminService.updateRequestStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Request status updated", response));
    }

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<User> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Users fetched", users));
    }

    // PUT /api/admin/users/{id}/toggle-status
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status toggled"));
    }

    // GET /api/admin/donors
    @GetMapping("/donors")
    public ResponseEntity<ApiResponse<Page<Donor>>> getAllDonors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Donor> donors = adminService.getAllDonors(page, size);
        return ResponseEntity.ok(ApiResponse.success("Donors fetched", donors));
    }

    // GET /api/admin/needy
    @GetMapping("/needy")
    public ResponseEntity<ApiResponse<Page<Needy>>> getAllNeedy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Needy> needy = adminService.getAllNeedy(page, size);
        return ResponseEntity.ok(ApiResponse.success("Needy users fetched", needy));
    }

    // GET /api/admin/feedback
    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<Page<Feedback>>> getAllFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Feedback> feedback = adminService.getAllFeedback(page, size);
        return ResponseEntity.ok(ApiResponse.success("Feedback fetched", feedback));
    }
}
