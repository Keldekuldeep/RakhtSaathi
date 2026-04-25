package com.rakhtsaathi.dto.response;

import com.rakhtsaathi.entity.enums.BloodGroup;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.entity.enums.UrgencyLevel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BloodRequestResponse {
    private Long id;
    // Frontend uses request.id.slice(-6) - need string version
    private String idStr;
    private String patientName;
    private BloodGroup bloodGroup;

    // Frontend uses both 'unitsNeeded' and 'unitsRequired' - provide both
    private Integer unitsNeeded;
    private Integer unitsRequired;

    private UrgencyLevel urgency;
    // Frontend uses 'urgencyLevel' in NeedyHistoryPage
    private UrgencyLevel urgencyLevel;

    private String hospital;
    // Frontend uses 'hospitalName' in NeedyHistoryPage
    private String hospitalName;

    private String city;
    private String attendantName;
    private String contactNumber;
    // Frontend also uses 'attendantPhone'
    private String attendantPhone;
    private String additionalNotes;
    private RequestStatus status;
    private Integer notifiedDonorsCount;
    private Integer acceptedDonorsCount;
    private Integer rejectedDonorsCount;

    // Voice message fields - used by DonorDashboardPage
    private String voiceMessageUrl;
    private Boolean hasVoiceMessage;

    // List format for admin/donor views
    private List<DonorNotificationResponse> donorNotifications;

    // Map format matching frontend's NeedyRequestStatusPage expected structure:
    // request.notifiedDonors[donorId] = { status, notifiedAt, respondedAt, donorInfo: {...} }
    private Map<String, DonorNotificationMapEntry> notifiedDonors;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime fulfilledAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class DonorNotificationMapEntry {
        private String status;
        private LocalDateTime notifiedAt;
        private LocalDateTime respondedAt;
        private DonorInfo donorInfo;

        @Getter @Setter
        @NoArgsConstructor @AllArgsConstructor
        @Builder
        public static class DonorInfo {
            private String fullName;
            private String bloodGroup;
            private String city;
            private String phone;
        }
    }
}
