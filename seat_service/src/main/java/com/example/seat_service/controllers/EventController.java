package com.example.seat_service.controllers;


import com.example.seat_service.dto.eventSeatStatus.EventSeatStatusResponse;
import com.example.seat_service.dto.event.EventRequest;
import com.example.seat_service.dto.event.EventResponse;
import com.example.seat_service.entity.EventStatus;
import com.example.seat_service.entity.SeatBookingStatus;
import com.example.seat_service.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // ─── ADMIN ENDPOINTS ────────────────────────────────────────────────────────

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.findAllByStatus(null));
    }

    @GetMapping("/admin/status")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> getByAnyStatus(@RequestParam EventStatus status) {
        return ResponseEntity.ok(eventService.findAllByStatus(status));
    }

    @GetMapping("/admin/venue/{venueId}/range")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> getByVenueAndRange(
            @PathVariable Long venueId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return ResponseEntity.ok(eventService.findAllByVenueIdAndStartTimeBetween(venueId, from, to));
    }

    // ─── USER ENDPOINTS ─────────────────────────────────────────────────────────

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<EventSeatStatusResponse>> getAllSeats(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.findAllSeatsByEventId(eventId));
    }

    @GetMapping("/{eventId}/seats/available")
    public ResponseEntity<List<EventSeatStatusResponse>> getAvailableSeats(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.findSeatsByEventIdAndStatus(eventId, SeatBookingStatus.AVAILABLE));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findEventById(id));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<EventResponse>> getByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(eventService.findAllByVenueId(venueId));
    }

    @GetMapping("/bookable")
    public ResponseEntity<List<EventResponse>> getCurrentlyBookable() {
        return ResponseEntity.ok(eventService.findAllCurrentlyBookable());
    }

    @GetMapping("/status")
    public ResponseEntity<List<EventResponse>> getByUserVisibleStatus(@RequestParam EventStatus status) {
        // users can only see ACTIVE and SOLD_OUT events
        if (status != EventStatus.ACTIVE && status != EventStatus.SOLD_OUT) {
            throw new IllegalArgumentException("Invalid status filter: " + status);
        }
        return ResponseEntity.ok(eventService.findAllByStatus(status));
    }

    @GetMapping("/range")
    public ResponseEntity<List<EventResponse>> getByStartTimeBetween(
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return ResponseEntity.ok(eventService.findAllByStartTimeBetween(from, to));
    }
}
