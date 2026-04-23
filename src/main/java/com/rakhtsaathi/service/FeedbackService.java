package com.rakhtsaathi.service;

import com.rakhtsaathi.dto.request.FeedbackRequest;
import com.rakhtsaathi.entity.*;
import com.rakhtsaathi.exception.ResourceNotFoundException;
import com.rakhtsaathi.repository.BloodRequestRepository;
import com.rakhtsaathi.repository.FeedbackRepository;
import com.rakhtsaathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public Feedback submitFeedback(User fromUser, FeedbackRequest request) {
        log.info("Submitting feedback for request: {}", request.getRequestId());

        BloodRequest bloodRequest = bloodRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodRequest", request.getRequestId()));

        Feedback feedback = Feedback.builder()
                .bloodRequest(bloodRequest)
                .fromUser(fromUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted with id: {}", saved.getId());
        return saved;
    }
}
