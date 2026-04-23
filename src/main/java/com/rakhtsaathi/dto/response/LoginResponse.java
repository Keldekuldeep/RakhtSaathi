package com.rakhtsaathi.dto.response;

import com.rakhtsaathi.entity.enums.UserType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private UserType userType;
}
