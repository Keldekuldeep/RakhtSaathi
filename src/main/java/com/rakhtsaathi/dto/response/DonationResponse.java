package com.rakhtsaathi.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DonationResponse {
    private Long id;
    private Long donorId;
    private String donorName;           // for certificate page
    private Long requestId;
    private String requestIdStr;        // string version for slice(-6)
    private String hospitalName;
    // Frontend certificate page uses 'hospital'
    private String hospital;
    private LocalDate donationDate;
    private String proofImageUrl;
    private String notes;
    private Integer units;
    private String status;
    private String certificateId;
    // Frontend certificate page uses 'certificateNumber'
    private String certificateNumber;
    // Frontend certificate page uses 'issuedDate'
    private LocalDate issuedDate;
    private LocalDateTime createdAt;
}
