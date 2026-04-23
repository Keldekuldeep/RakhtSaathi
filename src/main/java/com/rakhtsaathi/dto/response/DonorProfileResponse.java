package com.rakhtsaathi.dto.response;

import com.rakhtsaathi.entity.enums.BloodGroup;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DonorProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private BloodGroup bloodGroup;
    private String city;
    private String phone;
    private Integer age;
    private Double weight;
    private Boolean isAvailable;
    private Integer totalDonations;
    private Double rating;
    private LocalDate lastDonationDate;
    private LocalDateTime createdAt;
}
