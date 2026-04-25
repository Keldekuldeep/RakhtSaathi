package com.rakhtsaathi.dto.response;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    // Frontend uses these exact field names
    private long totalDonors;
    private long totalRequests;
    private long totalDonations;
    private long activeDonors;      // available donors
    private long activeRequests;
    private long fulfilledRequests;
    private long cancelledRequests;
    private long totalFeedback;
    private long totalUsers;
    private long totalNeedy;
}
