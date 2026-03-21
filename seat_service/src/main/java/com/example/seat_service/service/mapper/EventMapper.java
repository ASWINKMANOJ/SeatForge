package com.example.seat_service.service.mapper;

import com.example.seat_service.dto.event.EventRequest;
import com.example.seat_service.dto.event.EventResponse;
import com.example.seat_service.dto.event.EventCardResponse;
import com.example.seat_service.dto.event.EventDetailResponse;
import com.example.seat_service.dto.event.EventAdminResponse;
import com.example.seat_service.entity.Event;
import com.example.seat_service.entity.SeatBookingStatus;
import com.example.seat_service.entity.Venue;
import com.example.seat_service.repository.EventSeatStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final EventSeatStatusRepository eventSeatStatusRepository;

    public Event toEntity(EventRequest request, Venue venue) {
        Event event = new Event();
        event.setVenue(venue);
        updateEntity(event, request);   // reuse shared field mapping
        event.setCreatedAt(Instant.now());
        return event;
    }

    public void updateEntity(Event event, EventRequest request) {
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setBookingOpenAt(request.getBookingOpenAt());
        event.setBookingCloseAt(request.getBookingCloseAt());
        event.setStatus(request.getEventStatus());
        event.setImageUrl(request.getImageUrl());
        event.setCategory(request.getCategory());
        event.setIsFeatured(request.getIsFeatured());
        event.setIsSellingFast(request.getIsSellingFast());
        event.setStartingPrice(request.getStartingPrice());
    }

    public EventResponse toEventResponse(Event event) {
        return toEventResponse(event, eventSeatStatusRepository
                .countByEventIdAndStatus(event.getId(), SeatBookingStatus.AVAILABLE));
    }

    public EventResponse toEventResponse(Event event, Long availableSeats) {
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
                .availableSeats(availableSeats)
                .category(event.getCategory())
                .imageUrl(event.getImageUrl())
                .isFeatured(event.getIsFeatured())
                .isSellingFast(event.getIsSellingFast())
                .startingPrice(event.getStartingPrice())
                .build();
    }

    public EventCardResponse toCardResponse(Event event, Long availableSeats) {
        return EventCardResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .imageUrl(event.getImageUrl())
                .category(event.getCategory())
                .isFeatured(event.getIsFeatured())
                .isSellingFast(event.getIsSellingFast())
                .startingPrice(event.getStartingPrice())
                .availableSeats(availableSeats)
                .startTime(event.getStartTime())
                .venueName(event.getVenue().getName())
                .cityName(event.getVenue().getCity().getName())
                .eventStatus(event.getStatus())
                .build();
    }

    public EventDetailResponse toDetailResponse(Event event, Long availableSeats) {
        return EventDetailResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .category(event.getCategory())
                .isFeatured(event.getIsFeatured())
                .isSellingFast(event.getIsSellingFast())
                .startingPrice(event.getStartingPrice())
                .availableSeats(availableSeats)
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .bookingOpenAt(event.getBookingOpenAt())
                .bookingCloseAt(event.getBookingCloseAt())
                .eventStatus(event.getStatus())
                .venueId(event.getVenue().getId())
                .venueName(event.getVenue().getName())
                .venueAddress(event.getVenue().getAddress())
                .cityName(event.getVenue().getCity().getName())
                .cityState(event.getVenue().getCity().getState())
                .cityCountry(event.getVenue().getCity().getCountry())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

    public EventAdminResponse toAdminResponse(Event event, Long availableSeats, Long totalSeats) {
        return EventAdminResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .imageUrl(event.getImageUrl())
                .category(event.getCategory())
                .eventStatus(event.getStatus())
                .isFeatured(event.getIsFeatured())
                .isSellingFast(event.getIsSellingFast())
                .availableSeats(availableSeats)
                .totalSeats(totalSeats)
                .startingPrice(event.getStartingPrice())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .bookingOpenAt(event.getBookingOpenAt())
                .bookingCloseAt(event.getBookingCloseAt())
                .venueId(event.getVenue().getId())
                .venueName(event.getVenue().getName())
                .cityName(event.getVenue().getCity().getName())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
