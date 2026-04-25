package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.request.NeedyProfileRequest;
import com.rakhtsaathi.dto.response.ApiResponse;
import com.rakhtsaathi.dto.response.NeedyProfileResponse;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.service.AuthService;
import com.rakhtsaathi.service.NeedyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/needy")
@RequiredArgsConstructor
@Slf4j
public class NeedyController {

    private final NeedyService needyService;
    private final AuthService authService;

    // POST /api/needy/profile - Create profile after registration
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<NeedyProfileResponse>> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NeedyProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        NeedyProfileResponse response = needyService.createProfile(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Needy profile created successfully", response));
    }

    // GET /api/needy/profile - Get my profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<NeedyProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        NeedyProfileResponse response = needyService.getProfileResponse(user);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    // PUT /api/needy/profile - Update profile (city, phone, address, emergency contact)
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<NeedyProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NeedyProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        NeedyProfileResponse response = needyService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
