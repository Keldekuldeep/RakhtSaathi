package com.rakhtsaathi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "needy")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Needy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String relationToPatient;

    @Column(nullable = false)
    @Builder.Default
    private Integer requestCount = 0;

    @OneToMany(mappedBy = "needy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BloodRequest> bloodRequests;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
