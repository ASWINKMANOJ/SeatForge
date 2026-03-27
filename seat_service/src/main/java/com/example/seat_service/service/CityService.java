package com.example.seat_service.service;

import com.example.seat_service.dto.city.CityRequest;
import com.example.seat_service.dto.city.CityResponse;
import com.example.seat_service.entity.City;
import com.example.seat_service.repository.CityRepository;
import com.example.seat_service.service.mapper.CityMapper;
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
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Cacheable(value = "cities", key = "'all'")
    public List<CityResponse> getAllCities() {
        log.info("Cache MISS - fetching all cities from DB");
        return cityRepository.findAll()
                .stream()
                .map(cityMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "cities", key = "'state:' + #state")
    public List<CityResponse> getAllCitiesByState(String state) {
        log.info("Cache MISS - fetching cities by state:{} from DB", state);
        return cityRepository.findAllByStateOrderByNameAsc(state)
                .stream()
                .map(cityMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "cities", key = "#id")
    public CityResponse getCityById(Long id) {
        log.info("Cache MISS - fetching city id:{} from DB", id);
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No City with that id:" + id + " is saved in the database"));
        return cityMapper.toResponse(city);
    }

    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse createCity(CityRequest cityRequest) {
        if (cityRepository.existsByNameAndState(cityRequest.getName(), cityRequest.getState())) {
            throw new IllegalStateException("City already exists in this state");
        }

        return cityMapper.toResponse(cityRepository.save(cityMapper.toEntity(cityRequest)));

    }

    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse updateCity(Long id, CityRequest cityRequest) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No City with that id" + id + " is saved in the database"));
        existingCity.setName(cityRequest.getName());
        existingCity.setState(cityRequest.getState());
        existingCity.setCountry(cityRequest.getCountry());
        existingCity.setImageUrl(cityRequest.getImageUrl());
        existingCity.setTimezone(cityRequest.getTimeZone());
        return cityMapper.toResponse(cityRepository.save(existingCity));
    }

    @CacheEvict(value = "cities", allEntries = true)
    public void deleteCity(Long id) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No City with that id" + id + " is saved in the database"));
        cityRepository.delete(existingCity);
    }
}