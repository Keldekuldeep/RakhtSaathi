package com.rakhtsaathi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RecordDonationRequest {

    private Long requestId;

    @NotBlank(message = "Hospital name is required")
    private String hospitalName;

    @NotBlank(message = "Donation date is required")
    private String donationDate; // ISO date string

    private String imageUrl;

    private String notes;

    @NotNull
    private Integer units = 1;
}
