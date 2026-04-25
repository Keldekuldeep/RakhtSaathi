package com.rakhtsaathi.entity;

import com.rakhtsaathi.entity.enums.DonorResponseStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "donor_notifications",
    uniqueConstraints = @UniqueConstraint(columnNames = {"blood_request_id", "donor_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DonorNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blood_request_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private BloodRequest bloodRequest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Donor donor;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DonorResponseStatus status = DonorResponseStatus.NOTIFIED;

    private LocalDateTime notifiedAt;
    private LocalDateTime respondedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
