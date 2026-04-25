package com.rakhtsaathi.entity;

import com.rakhtsaathi.entity.enums.BloodGroup;
import com.rakhtsaathi.entity.enums.RequestStatus;
import com.rakhtsaathi.entity.enums.UrgencyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "blood_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "needy_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Needy needy;

    @Column(nullable = false)
    private String patientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private Integer unitsNeeded;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrgencyLevel urgency;

    @Column(nullable = false)
    private String hospital;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String attendantName;

    @Column(nullable = false)
    private String contactNumber;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    private String voiceMessageUrl;

    @Builder.Default
    private Boolean hasVoiceMessage = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.ACTIVE;

    @Builder.Default
    private Integer notifiedDonorsCount = 0;

    @Builder.Default
    private Integer acceptedDonorsCount = 0;

    @Builder.Default
    private Integer rejectedDonorsCount = 0;

    @OneToMany(mappedBy = "bloodRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DonorNotification> donorNotifications;

    @OneToMany(mappedBy = "bloodRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Feedback> feedbacks;

    private LocalDateTime fulfilledAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
