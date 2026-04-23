package com.rakhtsaathi.dto.response;

import com.rakhtsaathi.entity.enums.BloodGroup;
import com.rakhtsaathi.entity.enums.DonorResponseStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DonorNotificationResponse {
    private Long donorId;
    private String donorName;
    private BloodGroup bloodGroup;
    private String city;
    private String phone;
    private DonorResponseStatus status;
    private LocalDateTime notifiedAt;
    private LocalDateTime respondedAt;
}
