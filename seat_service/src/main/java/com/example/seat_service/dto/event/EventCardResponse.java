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
public class EventCardResponse implements Serializable {
    private Long id;
    private String title;
    private String imageUrl;
    private EventCategory category;
    private Boolean isFeatured;
    private Boolean isSellingFast;
    private BigDecimal startingPrice;
    private Long availableSeats;
    private Instant startTime;
    private String venueName;
    private String cityName;
    private EventStatus eventStatus;
}
