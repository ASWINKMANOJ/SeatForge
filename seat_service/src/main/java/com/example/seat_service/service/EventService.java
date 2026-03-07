package com.example.seat_service.service;

import com.example.seat_service.dto.EventSeatStatus.EventSeatStatusResponse;
import com.example.seat_service.dto.event.EventRequest;
import com.example.seat_service.dto.event.EventResponse;
import com.example.seat_service.entity.*;
import com.example.seat_service.repository.EventRepository;
import com.example.seat_service.repository.EventSeatStatusRepository;
import com.example.seat_service.repository.SeatRepository;
import com.example.seat_service.repository.VenueRepository;
import com.example.seat_service.service.mapper.EventMapper;
import com.example.seat_service.service.mapper.EventSeatStatusMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventSeatStatusRepository eventSeatStatusRepository;
    private final EventMapper eventMapper;
    private final EventSeatStatusMapper eventSeatStatusMapper;

    // fix N+1 — batch count for list endpoints
    private List<EventResponse> toResponseList(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> countMap = eventSeatStatusRepository
                .countAvailableSeatsForEvents(eventIds, SeatBookingStatus.AVAILABLE)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        return events.stream()
                .map(event -> eventMapper.toEventResponse(event, countMap.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    public List<EventResponse> findAllByVenueId(Long venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with id: " + venueId);
        }
        return toResponseList(eventRepository.findAllByVenueId(venueId));
    }

    public List<EventResponse> findAllByStartTimeBetween(Instant from, Instant to) {
        return toResponseList(eventRepository.findAllByStartTimeBetween(from, to));
    }

    public List<EventResponse> findAllByStatus(EventStatus status) {
        return toResponseList(eventRepository.findByStatus(status));
    }

    public List<EventResponse> findAllByVenueIdAndStartTimeBetween(Long venueId, Instant from, Instant to) {
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with id: " + venueId);
        }
        return toResponseList(eventRepository.findAllByVenueIdAndStartTimeBetween(venueId, from, to));
    }

    public List<EventResponse> findAllCurrentlyBookable() {
        return toResponseList(eventRepository.findAllCurrentlyBookable(Instant.now()));
    }

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Venue venue = venueRepository.findById(request.getVenue_id())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + request.getVenue_id()));

        validateBookingWindow(request);

        Event event = eventMapper.toEntity(request, venue);
        Event saved = eventRepository.save(event);

        initializeEventSeats(saved, venue.getId());

        return eventMapper.toEventResponse(saved, 0L);
    }

    public EventResponse findEventById(Long id) {
        return eventMapper.toEventResponse(
                eventRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id))
        );
    }

    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

        if (!existing.getVenue().getId().equals(request.getVenue_id())) {
            Venue newVenue = venueRepository.findById(request.getVenue_id())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + request.getVenue_id()));
            existing.setVenue(newVenue);
        }

        validateBookingWindow(request);
        eventMapper.updateEntity(existing, request);
        existing.setUpdatedAt(Instant.now());

        return eventMapper.toEventResponse(eventRepository.save(existing));
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found with id: " + id);
        }
        eventSeatStatusRepository.deleteAllByEventId(id);
        eventRepository.deleteById(id);
    }

    // ── seat availability ────────────────────────────────────────────────────

    public List<EventSeatStatusResponse> findAllSeatsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        return eventSeatStatusRepository.findAllByEventId(eventId)
                .stream()
                .map(eventSeatStatusMapper::toResponse)
                .toList();
    }

    public List<EventSeatStatusResponse> findSeatsByEventIdAndStatus(Long eventId, SeatBookingStatus status) {
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        return eventSeatStatusRepository.findAllByEventIdAndStatus(eventId, status)
                .stream()
                .map(eventSeatStatusMapper::toResponse)
                .toList();
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void initializeEventSeats(Event event, Long venueId) {
        List<Seat> seats = seatRepository.findAllWithVenue(venueId);

        if (seats.isEmpty()) {
            throw new IllegalStateException("Venue has no seats, cannot create event");
        }

        List<EventSeatStatus> eventSeats = seats.stream()
                .map(seat -> EventSeatStatus.builder()
                        .event(event)
                        .seat(seat)
                        .status(SeatBookingStatus.AVAILABLE)
                        .price(seat.getBasePrice())
                        .version(0)
                        .build())
                .toList();

        eventSeatStatusRepository.saveAll(eventSeats);
    }

    private void validateBookingWindow(EventRequest request) {
        if (request.getBookingOpenAt().isAfter(request.getStartTime())) {
            throw new IllegalStateException("Booking open time cannot be after event start time");
        }
        if (request.getBookingCloseAt().isAfter(request.getEndTime())) {
            throw new IllegalStateException("Booking close time cannot be after event end time");
        }
    }
}