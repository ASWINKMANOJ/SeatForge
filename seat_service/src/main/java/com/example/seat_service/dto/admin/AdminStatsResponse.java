package com.example.seat_service.dto.admin;

import com.example.seat_service.dto.venue.VenueCapacityResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    private Long activeLocksCount;
    private Long bookingsTodayCount;
    private BigDecimal currentRevenue;
    private List<VenueCapacityResponse> venueCapacities;
}

