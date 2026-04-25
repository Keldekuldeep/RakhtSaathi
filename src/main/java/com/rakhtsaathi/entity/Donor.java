package com.rakhtsaathi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rakhtsaathi.entity.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private String city;

    private String district;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private Double weight;

    private String gender;

    private String aadhaarNumber;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Builder.Default
    private Integer totalDonations = 0;

    // Frontend uses both 'donationCount' and 'totalDonations'
    @Builder.Default
    private Integer donationCount = 0;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer totalFeedbackCount = 0;

    private LocalDate lastDonationDate;

    @Builder.Default
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
