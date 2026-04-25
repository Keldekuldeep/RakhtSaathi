package com.rakhtsaathi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DonorRespondRequest {

    @NotBlank(message = "Response is required")
    private String response; // ACCEPTED or REJECTED
}
