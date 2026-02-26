package com.example.seat_service.service;

import com.example.seat_service.dto.city.CityRequest;
import com.example.seat_service.dto.city.CityResponse;
import com.example.seat_service.entity.City;
import com.example.seat_service.repository.CityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;

    public List<CityResponse> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(CityResponse::new)
                .toList();
    }

    public List<CityResponse> getAllCitiesByState(String state) {
        return cityRepository.findAllByStateOrderByNameAsc(state)
                .stream()
                .map(CityResponse::new)
                .toList();
    }

    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No City with that id is saved in the database"));
        return new CityResponse(city);
    }

    public CityResponse createCity(CityRequest cityRequest) {
        if (cityRepository.existsByNameAndState(cityRequest.getName(), cityRequest.getState())) {
            throw new IllegalStateException("City already exists in this state");
        }
        City newCity = new City();
        newCity.setName(cityRequest.getName());
        newCity.setState(cityRequest.getState());
        newCity.setCountry(cityRequest.getCountry());
        return new CityResponse(cityRepository.save(newCity));
    }

    public CityResponse updateCity(Long id, CityRequest cityRequest) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No City with that id is saved in the database"));
        existingCity.setName(cityRequest.getName());
        existingCity.setState(cityRequest.getState());
        existingCity.setCountry(cityRequest.getCountry());
        return new CityResponse(cityRepository.save(existingCity));
    }

    public void deleteCity(Long id) {
        City existingCity = cityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No City with that id is saved in the database"));
        cityRepository.delete(existingCity);
    }
}