package com.rakhtsaathi.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class NeedyProfileRequest {

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be at most 120")
    private Integer age;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Relation to patient is required")
    private String relationToPatient;

    // Optional fields for profile update
    private String phone;
    private String address;
    private String state;
    private String pincode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
}
