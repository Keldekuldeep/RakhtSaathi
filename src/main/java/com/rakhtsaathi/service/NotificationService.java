package com.rakhtsaathi.service;

import com.rakhtsaathi.entity.*;
import com.rakhtsaathi.entity.enums.BloodGroup;
import com.rakhtsaathi.entity.enums.DonorResponseStatus;
import com.rakhtsaathi.repository.BloodRequestRepository;
import com.rakhtsaathi.repository.DonorNotificationRepository;
import com.rakhtsaathi.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DonorRepository donorRepository;
    private final DonorNotificationRepository notificationRepository;
    private final BloodRequestRepository bloodRequestRepository;

    // Blood group compatibility map
    private static final Map<BloodGroup, List<BloodGroup>> COMPATIBILITY = new HashMap<>();

    static {
        COMPATIBILITY.put(BloodGroup.O_NEGATIVE, List.of(BloodGroup.O_NEGATIVE));
        COMPATIBILITY.put(BloodGroup.O_POSITIVE, List.of(BloodGroup.O_NEGATIVE, BloodGroup.O_POSITIVE));
        COMPATIBILITY.put(BloodGroup.A_NEGATIVE, List.of(BloodGroup.O_NEGATIVE, BloodGroup.A_NEGATIVE));
        COMPATIBILITY.put(BloodGroup.A_POSITIVE, List.of(BloodGroup.O_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.A_NEGATIVE, BloodGroup.A_POSITIVE));
        COMPATIBILITY.put(BloodGroup.B_NEGATIVE, List.of(BloodGroup.O_NEGATIVE, BloodGroup.B_NEGATIVE));
        COMPATIBILITY.put(BloodGroup.B_POSITIVE, List.of(BloodGroup.O_NEGATIVE, BloodGroup.O_POSITIVE, BloodGroup.B_NEGATIVE, BloodGroup.B_POSITIVE));
        COMPATIBILITY.put(BloodGroup.AB_NEGATIVE, List.of(BloodGroup.O_NEGATIVE, BloodGroup.A_NEGATIVE, BloodGroup.B_NEGATIVE, BloodGroup.AB_NEGATIVE));
        COMPATIBILITY.put(BloodGroup.AB_POSITIVE, Arrays.asList(BloodGroup.values())); // Universal recipient
    }

    @Async("notificationExecutor")
    @Transactional
    public CompletableFuture<Integer> notifyDonors(BloodRequest bloodRequest) {
        log.info("🔔 [ASYNC] Starting donor notification for request id: {}", bloodRequest.getId());

        try {
            List<BloodGroup> compatibleGroups = COMPATIBILITY.getOrDefault(
                    bloodRequest.getBloodGroup(), List.of(bloodRequest.getBloodGroup()));

            // Find compatible donors in same city
            List<Donor> donors = donorRepository.findCompatibleDonors(compatibleGroups, bloodRequest.getCity());
            log.info("Found {} compatible donors in {}", donors.size(), bloodRequest.getCity());

            if (donors.isEmpty()) {
                // Fallback: any available donor in city
                donors = donorRepository.findAvailableDonorsByCity(bloodRequest.getCity());
                log.info("Fallback: Found {} available donors in {}", donors.size(), bloodRequest.getCity());
            }

            int notifiedCount = 0;
            for (Donor donor : donors) {
                // Avoid duplicate notifications
                if (notificationRepository.findByBloodRequestAndDonor(bloodRequest, donor).isEmpty()) {
                    DonorNotification notification = DonorNotification.builder()
                            .bloodRequest(bloodRequest)
                            .donor(donor)
                            .status(DonorResponseStatus.NOTIFIED)
                            .notifiedAt(LocalDateTime.now())
                            .build();
                    notificationRepository.save(notification);
                    notifiedCount++;
                    log.info("Notified donor: {} ({})", donor.getUser().getFullName(), donor.getBloodGroup());
                }
            }

            // Update request counts
            bloodRequest.setNotifiedDonorsCount(notifiedCount);
            bloodRequestRepository.save(bloodRequest);

            log.info("✅ [ASYNC] Notification complete. Notified {} donors for request {}", notifiedCount, bloodRequest.getId());
            return CompletableFuture.completedFuture(notifiedCount);

        } catch (Exception e) {
            log.error("❌ [ASYNC] Notification failed for request {}: {}", bloodRequest.getId(), e.getMessage());
            return CompletableFuture.completedFuture(0);
        }
    }

    public List<BloodGroup> getCompatibleGroups(BloodGroup bloodGroup) {
        return COMPATIBILITY.getOrDefault(bloodGroup, List.of(bloodGroup));
    }
}
