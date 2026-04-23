package com.rakhtsaathi.dto.request;

import com.rakhtsaathi.entity.enums.BloodGroup;
import com.rakhtsaathi.entity.enums.UrgencyLevel;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BloodRequestDTO {

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotNull(message = "Units needed is required")
    @Min(value = 1, message = "At least 1 unit required")
    @Max(value = 10, message = "Maximum 10 units allowed")
    private Integer unitsNeeded;

    @NotNull(message = "Urgency level is required")
    private UrgencyLevel urgency;

    @NotBlank(message = "Hospital name is required")
    private String hospital;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Attendant name is required")
    private String attendantName;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Invalid phone number")
    private String contactNumber;

    private String additionalNotes;
}
