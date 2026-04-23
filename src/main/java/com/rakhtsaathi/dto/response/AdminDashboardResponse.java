package com.rakhtsaathi.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalDonors;
    private long totalNeedy;
    private long totalRequests;
    private long activeRequests;
    private long fulfilledRequests;
    private long cancelledRequests;
    private long availableDonors;
    private long totalFeedback;
}
