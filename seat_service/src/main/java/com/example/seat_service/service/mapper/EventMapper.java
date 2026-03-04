package com.example.seat_service.service.mapper;

import com.example.seat_service.dto.event.EventRequest;
import com.example.seat_service.dto.event.EventResponse;
import com.example.seat_service.entity.Event;
import com.example.seat_service.entity.Venue;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class EventMapper {
    public Event toEntity(EventRequest request, Venue venue) {
        Event event = new Event();
        event.setVenue(venue);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setBookingOpenAt(request.getBookingOpenAt());
        event.setBookingCloseAt(request.getBookingCloseAt());
        event.setStatus(request.getEventStatus());
        event.setCreatedAt(Instant.now());

        return event;
    }

    public EventResponse toEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .venue_id(event.getVenue().getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .bookingOpenAt(event.getBookingOpenAt())
                .bookingCloseAt(event.getBookingCloseAt())
                .eventStatus(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }


}
