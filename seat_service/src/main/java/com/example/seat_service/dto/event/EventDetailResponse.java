package com.example.seat_service.dto.event;

import com.example.seat_service.entity.EventCategory;
import com.example.seat_service.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDetailResponse implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private EventCategory category;
    private Boolean isFeatured;
    private Boolean isSellingFast;
    private BigDecimal startingPrice;
    private Long availableSeats;
    private Instant startTime;
    private Instant endTime;
    private Instant bookingOpenAt;
    private Instant bookingCloseAt;
    private EventStatus eventStatus;
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private String cityName;
    private String cityState;
    private String cityCountry;
    private Instant createdAt;
    private Instant updatedAt;
}
