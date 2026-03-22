package com.example.seat_service.dto.venue;

import com.example.seat_service.entity.VenueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

// VenueResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse implements Serializable {
    private Long id;
    private String name;
    private String address;
    private Long city_id;
    private String city;
    private VenueType type;
    private Integer totalCapacity;
    private Boolean isActive;
    private Instant createdAt;

}
