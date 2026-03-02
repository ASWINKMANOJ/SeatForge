package com.example.seat_service.dto.venue;

import com.example.seat_service.entity.VenueType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueRequest {
    private String name;
    private String address;
    private Long city_id;
    private VenueType venue_type;
    private Integer totalCapacity;
    private Boolean isActive;
}
