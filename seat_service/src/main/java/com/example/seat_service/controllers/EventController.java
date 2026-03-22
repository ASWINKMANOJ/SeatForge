package com.example.seat_service.controllers;

import com.example.seat_service.dto.eventSeatStatus.EventSeatStatusResponse;
import com.example.seat_service.dto.event.EventRequest;
import com.example.seat_service.dto.event.EventCardResponse;
import com.example.seat_service.dto.event.EventDetailResponse;
import com.example.seat_service.dto.event.EventAdminResponse;
import com.example.seat_service.entity.EventStatus;
import com.example.seat_service.entity.EventCategory;
import com.example.seat_service.entity.SeatBookingStatus;
import com.example.seat_service.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('admin:events')")
    public ResponseEntity<EventDetailResponse> createEvent(@Valid @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:events')")
    public ResponseEntity<EventDetailResponse> updateEvent(@PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:events')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('admin:all')")
    public ResponseEntity<List<EventAdminResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.findAllByStatusAdmin(null));
    }

    @GetMapping("/admin/status")
    @PreAuthorize("hasAuthority('admin:all')")
    public ResponseEntity<List<EventAdminResponse>> getByAnyStatus(@RequestParam EventStatus status) {
        return ResponseEntity.ok(eventService.findAllByStatusAdmin(status));
    }

    @GetMapping("/admin/venue/{venueId}/range")
    @PreAuthorize("hasAuthority('admin:all')")
    public ResponseEntity<List<EventAdminResponse>> getByVenueAndRange(
            @PathVariable Long venueId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return ResponseEntity.ok(eventService.findAllByVenueIdAndStartTimeBetweenAdmin(venueId, from, to));
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
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findEventById(id));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<EventCardResponse>> getByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(eventService.findAllByVenueId(venueId));
    }

    @GetMapping("/bookable")
    public ResponseEntity<List<EventCardResponse>> getCurrentlyBookable() {
        return ResponseEntity.ok(eventService.findAllCurrentlyBookable());
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventCardResponse>> search(
            @RequestParam String query,
            @RequestParam(required = false) String city) {
        return ResponseEntity.ok(eventService.searchEvents(query, city));
    }

    @GetMapping("/status")
    public ResponseEntity<List<EventCardResponse>> getByUserVisibleStatus(
            @RequestParam EventStatus status,
            @RequestParam(required = false) EventCategory category) {
        if (status != EventStatus.ACTIVE && status != EventStatus.SOLD_OUT) {
            throw new IllegalArgumentException("Invalid status filter: " + status);
        }
        return ResponseEntity.ok(eventService.findAllByStatus(status, category));
    }

    @GetMapping("/range")
    public ResponseEntity<List<EventCardResponse>> getByStartTimeBetween(
            @RequestParam Instant from,
            @RequestParam Instant to) {
        return ResponseEntity.ok(eventService.findAllByStartTimeBetween(from, to));
    }
}