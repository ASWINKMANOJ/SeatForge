package com.example.seat_service.controllers;

import com.example.seat_service.dto.city.CityRequest;
import com.example.seat_service.dto.city.CityResponse;
import com.example.seat_service.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public ResponseEntity<List<CityResponse>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<CityResponse>> getCitiesByState(@RequestParam String state) {
        return ResponseEntity.ok(cityService.getAllCitiesByState(state));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityResponse> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CityRequest cityRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cityService.createCity(cityRequest));
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityResponse> updateCity(@PathVariable Long id, @Valid @RequestBody CityRequest cityRequest) {
        return ResponseEntity.ok(cityService.updateCity(id, cityRequest));
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}