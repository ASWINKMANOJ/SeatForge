package com.example.seat_service.controllers;

import com.example.seat_service.dto.booking.BookingRequest;
import com.example.seat_service.dto.booking.BookingResponse;
import com.example.seat_service.dto.booking.BookingDetailResponse;
import com.example.seat_service.dto.booking.BookingSummaryResponse;
import com.example.seat_service.dto.lock.LockRequest;
import com.example.seat_service.dto.lock.LockResponse;
import com.example.seat_service.service.BookingService;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @Valid @RequestBody LockRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(bookingService.lockSeats(request, jwt.getSubject()));
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
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, jwt.getSubject()));
    }

    // CANCEL BOOKING
    @DeleteMapping("/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, jwt.getSubject()));
    }

    // GET BOOKING BY ID
    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // GET BOOKINGS BY USER
    @GetMapping("/user")
    public ResponseEntity<List<BookingSummaryResponse>> getBookingsByUser(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(bookingService.getBookingsByUser(jwt.getSubject()));
    }
}