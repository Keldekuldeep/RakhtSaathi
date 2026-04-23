package com.rakhtsaathi.dto.response;

import com.rakhtsaathi.entity.enums.BloodGroup;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.entity.enums.UrgencyLevel;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BloodRequestResponse {
    private Long id;
    private String patientName;
    private BloodGroup bloodGroup;
    private Integer unitsNeeded;
    private UrgencyLevel urgency;
    private String hospital;
    private String city;
    private String attendantName;
    private String contactNumber;
    private String additionalNotes;
    private RequestStatus status;
    private Integer notifiedDonorsCount;
    private Integer acceptedDonorsCount;
    private Integer rejectedDonorsCount;
    private List<DonorNotificationResponse> donorNotifications;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
