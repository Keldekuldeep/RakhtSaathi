package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.request.NeedyProfileRequest;
import com.rakhtsaathi.dto.response.ApiResponse;
import com.rakhtsaathi.entity.Needy;
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

    // POST /api/needy/profile
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<Long>> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NeedyProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        Needy needy = needyService.createProfile(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Needy profile created successfully", needy.getId()));
    }

    // GET /api/needy/profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Needy>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        Needy needy = needyService.getProfile(user);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", needy));
    }

    // PUT /api/needy/profile
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Long>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NeedyProfileRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        Needy needy = needyService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", needy.getId()));
    }
}
