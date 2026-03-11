package com.example.seat_service.controllers;


import com.example.seat_service.dto.venue.VenueRequest;
import com.example.seat_service.dto.venue.VenueResponse;
import com.example.seat_service.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getVenues(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive) {

        if (cityId != null && isActive != null) {
            return ResponseEntity.ok(venueService.findByCityIdAndIsActive(cityId, isActive));
        } else if (cityId != null) {
            return ResponseEntity.ok(venueService.findByCityId(cityId));
        } else if (isActive != null) {
            return ResponseEntity.ok(venueService.findByIsActive(isActive));
        }
        return ResponseEntity.ok(venueService.findAllVenues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.findById(id));
    }

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(request));
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
