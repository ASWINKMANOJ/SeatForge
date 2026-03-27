package com.example.seat_service.service.mapper;


import com.example.seat_service.dto.city.CityRequest;
import com.example.seat_service.dto.city.CityResponse;
import com.example.seat_service.entity.City;
import com.example.seat_service.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CityMapper {

    private final CityRepository cityRepository;

    public City toEntity(CityRequest cityRequest) {
        City city = new City();
        city.setName(cityRequest.getName());
        city.setState(cityRequest.getState());
        city.setCountry(cityRequest.getCountry());

        return cityRepository.save(city);
    }

    public CityResponse toResponse(City city) {
        CityResponse cityResponse = new CityResponse();
        cityResponse.setId(city.getId());
        cityResponse.setName(city.getName());
        cityResponse.setState(city.getState());
        cityResponse.setCountry(city.getCountry());
        return cityResponse;
    }
}
