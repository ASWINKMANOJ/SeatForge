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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final VenueMapper venueMapper;
    private final CityRepository cityRepository;

    @Cacheable(value = "venues", key = "'all'")
    public List<VenueResponse> findAllVenues() {
        log.info("Cache MISS - fetching all venues from DB");
        return venueRepository.findAllWithCity()
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "venues", key = "'city:' + #cityId")
    public List<VenueResponse> findByCityId(Long cityId) {
        log.info("Cache MISS - fetching venues by cityId:{} from DB", cityId);
        return venueRepository.findAllByCityId(cityId)
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "venues", key = "'active:' + #isActive")
    public List<VenueResponse> findByIsActive(Boolean active) {
        log.info("Cache MISS - fetching venues by isActive:{} from DB", active);
        return venueRepository.findAllByIsActive(active)
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "venues", key = "'city:' + #cityId + ':active:' + #isActive")
    public List<VenueResponse> findByCityIdAndIsActive(Long cityId, Boolean active) {
        log.info("Cache MISS - fetching venues by cityId:{} isActive:{} from DB", cityId, active);
        return venueRepository.findAllByCityIdAndIsActive(cityId, active)
                .stream()
                .map(venueMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "venues", key = "#id")
    public VenueResponse findById(Long id) {
        log.info("Cache MISS - fetching venue id:{} from DB", id);
        return venueMapper.toResponse(venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Venue with that id:" + id + " is saved in the database")));
    }

    @CacheEvict(value = "venues", allEntries = true)
    public VenueResponse createVenue(VenueRequest request) {
        City city = cityRepository.findById(request.getCity_id())
                .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + request.getCity_id()));

        if (venueRepository.existsByNameAndCity_Id(request.getName(), request.getCity_id())) {
            throw new IllegalStateException("Venue '" + request.getName() + "' already exists in this city");
        }
        Venue venue = venueMapper.toEntity(request, city);
        return venueMapper.toResponse(venueRepository.save(venue));
    }

    @CacheEvict(value = "venues", allEntries = true)
    public VenueResponse updateVenue(Long id, VenueRequest request) {
        Venue existing = venueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Venue with that id: " + id));

        if (!existing.getCity().getId().equals(request.getCity_id())) {
            City newCity = cityRepository.findById(request.getCity_id())
                    .orElseThrow(() -> new EntityNotFoundException("City not found with id: " + request.getCity_id()));
            existing.setCity(newCity);
        }

        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setType(request.getVenue_type());
        existing.setImageUrl(request.getImageUrl());
        existing.setTotalCapacity(request.getTotalCapacity());
        existing.setActive(request.getActive());

        return venueMapper.toResponse(venueRepository.save(existing));
    }

    @CacheEvict(value = "venues", allEntries = true)
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new EntityNotFoundException("No Venue with that id: " + id);
        }
        venueRepository.deleteById(id);
    }
}
