package com.example.seat_service.dto.event;

import com.example.seat_service.entity.EventCategory;
import com.example.seat_service.entity.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest {
    @NotNull(message = "Venue id is required")
    private Long venue_id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    @NotNull(message = "End time is required")
    private Instant endTime;

    @NotNull(message = "Booking open time is required")
    private Instant bookingOpenAt;

    @NotNull(message = "Booking close time is required")
    private Instant bookingCloseAt;

    @NotNull(message = "Event status is required")
    private EventStatus eventStatus;

    @NotBlank(message = "Image url is required")
    private String imageUrl;

    @NotNull(message = "Event category is required")
    private EventCategory category;

    private Boolean isFeatured;

    private Boolean isSellingFast;

    private BigDecimal startingPrice;
}
