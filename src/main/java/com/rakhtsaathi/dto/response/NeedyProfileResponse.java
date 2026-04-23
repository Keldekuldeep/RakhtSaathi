package com.rakhtsaathi.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NeedyProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String city;
    private Integer age;
    private String gender;
    private String relationToPatient;
    private Integer requestCount;
    private LocalDateTime createdAt;
}
