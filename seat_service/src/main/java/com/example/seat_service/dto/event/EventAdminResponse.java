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
public class EventAdminResponse implements Serializable {
    private Long id;
    private String title;
    private String imageUrl;
    private EventCategory category;
    private EventStatus eventStatus;
    private Boolean isFeatured;
    private Boolean isSellingFast;
    private Long availableSeats;
    private Long totalSeats;
    private BigDecimal startingPrice;
    private Instant startTime;
    private Instant endTime;
    private Instant bookingOpenAt;
    private Instant bookingCloseAt;
    private Long venueId;
    private String venueName;
    private String cityName;
    private Instant createdAt;
    private Instant updatedAt;
}
