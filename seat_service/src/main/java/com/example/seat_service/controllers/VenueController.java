package com.example.seat_service.controllers;


import com.example.seat_service.dto.city.CityResponse;
import com.example.seat_service.dto.venue.VenueRequest;
import com.example.seat_service.dto.venue.VenueResponse;
import com.example.seat_service.service.VenueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

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
    public ResponseEntity<VenueResponse> createVenue(@RequestBody VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long id, @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
