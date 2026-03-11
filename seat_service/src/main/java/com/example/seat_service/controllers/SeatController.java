package com.example.seat_service.controllers;

import com.example.seat_service.dto.seat.SeatRequest;
import com.example.seat_service.dto.seat.SeatResponse;
import com.example.seat_service.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<List<SeatResponse>> getAllSeatsByVenue(@PathVariable Long venueId) {
        return ResponseEntity.ok(seatService.findAllSeatByVenue(venueId));
    }

    @GetMapping("/{seatId}")
    public ResponseEntity<SeatResponse> getSeatById(@PathVariable Long seatId) {
        return ResponseEntity.ok(seatService.findSeatById(seatId));
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatResponse> createSeat(@Valid @RequestBody SeatRequest seatRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seatService.createSeat(seatRequest));
    }

    @PutMapping("/{seatId}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeatResponse> updateSeat(@PathVariable Long seatId, @Valid @RequestBody SeatRequest seatRequest) {
        return ResponseEntity.ok(seatService.updateSeat(seatId, seatRequest));
    }

    @DeleteMapping("/{seatId}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long seatId) {
        seatService.deleteSeat(seatId);
        return ResponseEntity.noContent().build();
    }
}