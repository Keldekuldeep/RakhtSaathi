package com.rakhtsaathi.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminFeedbackResponse {
    private Long id;
    private Long requestId;
    private Integer rating;
    private String text;
    private String comment;         // alias for text
    // Frontend uses donorName and needyName
    private String donorName;
    private String needyName;
    private String fromUserName;
    private String toUserName;
    private LocalDateTime createdAt;
}
