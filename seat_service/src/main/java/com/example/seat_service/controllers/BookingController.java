package com.example.seat_service.controllers;

import com.example.seat_service.dto.booking.BookingRequest;
import com.example.seat_service.dto.booking.BookingResponse;
import com.example.seat_service.dto.lock.LockRequest;
import com.example.seat_service.dto.lock.LockResponse;
import com.example.seat_service.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// BookingController.java
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // LOCK SEATS
    @PostMapping("/lock")
    public ResponseEntity<LockResponse> lockSeats(
            @RequestBody LockRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(bookingService.lockSeats(request, userId));
    }

    // UNLOCK SEATS
    @DeleteMapping("/lock")
    public ResponseEntity<Void> unlockSeats(
            @RequestBody List<Long> eventSeatStatusIds) {
        bookingService.unlockSeats(eventSeatStatusIds);
        return ResponseEntity.noContent().build();
    }

    // CREATE BOOKING
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, userId));
    }

    // CANCEL BOOKING
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, userId));
    }

    // GET BOOKING BY ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // GET BOOKINGS BY USER
    @GetMapping("/user")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }
}