package com.example.seat_service.service;

import com.example.seat_service.dto.event.EventRequest;
import com.example.seat_service.dto.event.EventResponse;
import com.example.seat_service.entity.Event;
import com.example.seat_service.entity.EventStatus;
import com.example.seat_service.entity.Venue;
import com.example.seat_service.repository.EventRepository;
import com.example.seat_service.repository.VenueRepository;
import com.example.seat_service.service.mapper.EventMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final EventMapper eventMapper;

    public List<EventResponse> findAllByVenueId(Long venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with id: " + venueId);
        }
        return eventRepository.findAllByVenueId(venueId)
                .stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

    public List<EventResponse> findAllByStartTimeBetween(Instant from, Instant to) {
        return eventRepository.findAllByStartTimeBetween(from, to)
                .stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

    public List<EventResponse> findAllByStatus(EventStatus status) {
        return eventRepository.findByStatus(status)
                .stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

    public List<EventResponse> findAllByVenueIdAndStartTimeBetween(Long venueId, Instant from, Instant to) {
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with id: " + venueId);
        }
        return eventRepository.findAllByVenueIdAndStartTimeBetween(venueId, from, to)
                .stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

    public List<EventResponse> findAllCurrentlyBookable() {
        return eventRepository.findAllCurrentlyBookable(Instant.now())
                .stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

    public EventResponse createEvent(EventRequest request) {
        Venue venue = venueRepository.findById(request.getVenue_id())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + request.getVenue_id()));

        // validate booking window is within event time
        if (request.getBookingOpenAt().isAfter(request.getStartTime())) {
            throw new IllegalStateException("Booking open time cannot be after event start time");
        }
        if (request.getBookingCloseAt().isAfter(request.getEndTime())) {
            throw new IllegalStateException("Booking close time cannot be after event end time");
        }

        Event event = eventMapper.toEntity(request, venue);
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    // READ BY ID
    public EventResponse findEventById(Long id) {
        return eventMapper.toEventResponse(
                eventRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id))
        );
    }

    // UPDATE
    public EventResponse updateEvent(Long id, EventRequest request) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

        // update venue only if venueId changed
        if (!existing.getVenue().getId().equals(request.getVenue_id())) {
            Venue newVenue = venueRepository.findById(request.getVenue_id())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + request.getVenue_id()));
            existing.setVenue(newVenue);
        }

        // validate booking window
        if (request.getBookingOpenAt().isAfter(request.getStartTime())) {
            throw new IllegalStateException("Booking open time cannot be after event start time");
        }
        if (request.getBookingCloseAt().isAfter(request.getEndTime())) {
            throw new IllegalStateException("Booking close time cannot be after event end time");
        }

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setStartTime(request.getStartTime());
        existing.setEndTime(request.getEndTime());
        existing.setBookingOpenAt(request.getBookingOpenAt());
        existing.setBookingCloseAt(request.getBookingCloseAt());
        existing.setStatus(request.getEventStatus());
        existing.setUpdatedAt(Instant.now());

        return eventMapper.toEventResponse(eventRepository.save(existing));
    }
    // DELETE
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }
}
