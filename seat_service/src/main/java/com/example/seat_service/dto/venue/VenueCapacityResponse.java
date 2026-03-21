package com.example.seat_service.dto.venue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueCapacityResponse {
    private String venueName;
    private Integer totalCapacity;
    private Long bookedSeats;
    private Integer occupancyPercentage;
}

