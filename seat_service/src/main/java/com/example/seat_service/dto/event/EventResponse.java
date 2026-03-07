package com.example.seat_service.dto.event;

import com.example.seat_service.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private Long venue_id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Instant bookingOpenAt;
    private Instant bookingCloseAt;
    private EventStatus eventStatus;
    private Instant createdAt;
    private Instant updatedAt;
    private Long availableSeats;
}
