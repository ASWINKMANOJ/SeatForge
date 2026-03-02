package com.example.seat_service.service;

import com.example.seat_service.dto.venue.VenueRequest;
import com.example.seat_service.dto.venue.VenueResponse;
import com.example.seat_service.entity.City;
import com.example.seat_service.entity.Venue;
import com.example.seat_service.repository.CityRepository;
import com.example.seat_service.repository.VenueRepository;
import com.example.seat_service.service.mapper.VenueMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;
    private final CityRepository cityRepository;

    public List<VenueResponse> findAllVenues() {
        return venueRepository.findAllWithCity()
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    public List<VenueResponse> findByCityId(Long cityId) {
        return venueRepository.findAllByCityId(cityId)
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    public List<VenueResponse> findByIsActive(Boolean isActive) {
        return venueRepository.findAllByIsActive(isActive)
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    public List<VenueResponse> findByCityIdAndIsActive(Long cityId, Boolean isActive) {
        return venueRepository.findAllByCityIdAndIsActive(cityId, isActive)
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    public VenueResponse findById(Long id) {
        return venueMapper.toResponse(venueRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("No Venue with that id:" + id + " is saved in the database ")));
    }

    public VenueResponse createVenue(VenueRequest request) {
        City city = cityRepository.findById(request.getCity_id())
                .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + request.getCity_id()));

        if(venueRepository.existsByNameAndCity_Id(request.getName(), request.getCity_id())) {
            throw new IllegalStateException("Venue '" + request.getName() + "' already exists in this city");
        }
        Venue venue = venueMapper.toEntity(request, city);
        return venueMapper.toResponse(venueRepository.save(venue));
    }

    public VenueResponse updateVenue(Long id, VenueRequest request) {
        Venue existing = venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Venue with that id: " + id));

        // update city only if cityId changed
        if (!existing.getCity().getId().equals(request.getCity_id())) {
            City newCity = cityRepository.findById(request.getCity_id())
                    .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + request.getCity_id()));
            existing.setCity(newCity);
        }

        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setType(request.getVenue_type());
        existing.setTotalCapacity(request.getTotalCapacity());
        existing.setIsActive(request.getIsActive());

        return venueMapper.toResponse(venueRepository.save(existing));
    }

    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new EntityNotFoundException("No Venue with that id: " + id);
        }
        venueRepository.deleteById(id);
    }
}
