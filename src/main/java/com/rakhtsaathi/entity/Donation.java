package com.rakhtsaathi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Donor donor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blood_request_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private BloodRequest bloodRequest;

    @Column(nullable = false)
    private String hospitalName;

    @Column(nullable = false)
    private LocalDate donationDate;

    private String proofImageUrl;

    private String notes;

    @Builder.Default
    private Integer units = 1;

    // PENDING, VERIFIED, REJECTED
    @Builder.Default
    private String status = "PENDING";

    private String certificateId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
