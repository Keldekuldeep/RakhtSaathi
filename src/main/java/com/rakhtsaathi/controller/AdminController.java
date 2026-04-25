package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.response.*;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // GET /api/admin/dashboard
    // Frontend: getAnalytics() → analytics.totalDonors, totalRequests, totalDonations, activeDonors, activeRequests
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched", dashboard));
    }

    // GET /api/admin/requests?status=all&page=0&size=20
    // Frontend: getAllRequests() - all blood requests with filters
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<BloodRequestResponse>>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL") String status) {

        Page<BloodRequestResponse> requests = adminService.getAllRequests(page, size, status);
        return ResponseEntity.ok(ApiResponse.success("Requests fetched", requests));
    }

    // PUT /api/admin/requests/{id}/status?status=FULFILLED
    // Frontend: updateRequestStatus(requestId, newStatus)
    @PutMapping("/requests/{id}/status")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        BloodRequestResponse response = adminService.updateRequestStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Request status updated to " + status, response));
    }

    // GET /api/admin/donors?page=0&size=20
    // Frontend: getAllDonors() - all donors with filters
    @GetMapping("/donors")
    public ResponseEntity<ApiResponse<Page<AdminDonorResponse>>> getAllDonors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminDonorResponse> donors = adminService.getAllDonors(page, size);
        return ResponseEntity.ok(ApiResponse.success("Donors fetched", donors));
    }

    // PUT /api/admin/donors/{id}/availability
    // Frontend: updateDonorStatus(donorId, !currentStatus)
    @PutMapping("/donors/{id}/availability")
    public ResponseEntity<ApiResponse<AdminDonorResponse>> toggleDonorAvailability(
            @PathVariable Long id) {

        AdminDonorResponse response = adminService.toggleDonorAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Donor availability updated to: " + response.getIsAvailable(), response));
    }

    // GET /api/admin/feedback
    // Frontend: getFeedback() - all feedback, filter suspicious (rating <= 2)
    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<Page<AdminFeedbackResponse>>> getAllFeedback(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminFeedbackResponse> feedback = adminService.getAllFeedback(page, size);
        return ResponseEntity.ok(ApiResponse.success("Feedback fetched", feedback));
    }

    // GET /api/admin/donations/pending
    // Frontend: getCertificates() - pending donation proofs for approval
    @GetMapping("/donations/pending")
    public ResponseEntity<ApiResponse<List<DonationResponse>>> getPendingDonations() {
        List<DonationResponse> donations = adminService.getPendingDonations();
        return ResponseEntity.ok(ApiResponse.success("Pending donations fetched", donations));
    }

    // PUT /api/admin/donations/{id}/approve
    // Frontend: handleApproveCertificate(certificateId)
    @PutMapping("/donations/{id}/approve")
    public ResponseEntity<ApiResponse<DonationResponse>> approveDonation(@PathVariable Long id) {
        DonationResponse response = adminService.approveDonation(id);
        return ResponseEntity.ok(ApiResponse.success("Donation approved", response));
    }

    // PUT /api/admin/donations/{id}/reject
    @PutMapping("/donations/{id}/reject")
    public ResponseEntity<ApiResponse<DonationResponse>> rejectDonation(@PathVariable Long id) {
        DonationResponse response = adminService.rejectDonation(id);
        return ResponseEntity.ok(ApiResponse.success("Donation rejected", response));
    }

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<User> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Users fetched", users));
    }

    // PUT /api/admin/users/{id}/toggle-status
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status toggled"));
    }
}
