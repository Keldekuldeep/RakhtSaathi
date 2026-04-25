package com.rakhtsaathi.dto.response;

import com.rakhtsaathi.entity.enums.BloodGroup;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminDonorResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String name;            // alias
    private String email;
    private BloodGroup bloodGroup;
    private String city;
    private String district;
    private String phone;
    private String contactNumber;   // alias
    private Integer age;
    private Double weight;
    private String gender;
    private Boolean isAvailable;
    private Integer totalDonations;
    private Integer donationCount;  // alias
    private Double rating;
    private Integer totalFeedbackCount;
    private LocalDate lastDonationDate;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
