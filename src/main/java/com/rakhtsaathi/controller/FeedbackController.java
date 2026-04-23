package com.rakhtsaathi.controller;

import com.rakhtsaathi.dto.request.FeedbackRequest;
import com.rakhtsaathi.dto.response.ApiResponse;
import com.rakhtsaathi.entity.Feedback;
import com.rakhtsaathi.entity.User;
import com.rakhtsaathi.service.AuthService;
import com.rakhtsaathi.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Slf4j
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final AuthService authService;

    // POST /api/feedback
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> submitFeedback(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FeedbackRequest request) {

        User user = authService.getCurrentUser(userDetails.getUsername());
        Feedback feedback = feedbackService.submitFeedback(user, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", feedback.getId()));
    }
}
