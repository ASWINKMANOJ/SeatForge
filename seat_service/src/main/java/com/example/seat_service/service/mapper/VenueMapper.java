package com.example.seat_service.service.mapper;

import com.example.seat_service.dto.venue.VenueRequest;
import com.example.seat_service.dto.venue.VenueResponse;
import com.example.seat_service.entity.City;
import com.example.seat_service.entity.Venue;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class VenueMapper {

    public VenueResponse toResponse(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city_id(venue.getCity().getId())
                .city(venue.getCity().getName())
                .type(venue.getType())
                .totalCapacity(venue.getTotalCapacity())
                .imageUrl(venue.getImageUrl())
                .isActive(venue.getIsActive())
                .createdAt(venue.getCreatedAt())
                .build();
    }

    public Venue toEntity(VenueRequest request, City city) {
        Venue venue = new Venue();
        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(city);
        venue.setType(request.getVenue_type());
        venue.setTotalCapacity(request.getTotalCapacity());
        venue.setImageUrl(request.getImageUrl());
        venue.setIsActive(request.getIsActive());
        venue.setCreatedAt(Instant.now());
        return venue;
    }
}
