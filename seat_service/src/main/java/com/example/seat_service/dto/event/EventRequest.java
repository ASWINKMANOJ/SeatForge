package com.example.seat_service.dto.event;

import com.example.seat_service.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    private Long venue_id;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Instant bookingOpenAt;
    private Instant bookingCloseAt;
    private EventStatus eventStatus;
}
