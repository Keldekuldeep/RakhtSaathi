package com.rakhtsaathi.dto.request;

import com.rakhtsaathi.entity.enums.BloodGroup;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DonorProfileRequest {

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Invalid phone number")
    private String phone;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Minimum age is 18")
    @Max(value = 65, message = "Maximum age is 65")
    private Integer age;

    @NotNull(message = "Weight is required")
    @Min(value = 50, message = "Minimum weight is 50 kg")
    private Double weight;

    private Boolean isAvailable = true;
}
