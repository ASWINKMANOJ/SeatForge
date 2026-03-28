package com.example.seat_service.controllers;


import com.example.seat_service.dto.venue.VenueRequest;
import com.example.seat_service.dto.venue.VenueResponse;
import com.example.seat_service.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestParam(required = false, defaultValue = "true") Boolean active) {

        if (cityId != null && active != null) {
            return ResponseEntity.ok(venueService.findByCityIdAndIsActive(cityId, active));
        } else if (cityId != null) {
            return ResponseEntity.ok(venueService.findByCityId(cityId));
        } else if (active != null) {
            return ResponseEntity.ok(venueService.findByIsActive(active));
        }
        return ResponseEntity.ok(venueService.findAllVenues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:venues')")
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:venues')")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long id, @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:venues')")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
