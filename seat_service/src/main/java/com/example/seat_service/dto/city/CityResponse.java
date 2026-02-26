package com.example.seat_service.dto.city;

import com.example.seat_service.entity.City;
import lombok.Getter;

@Getter
public class CityResponse {
    private final Long id;
    private final String name;
    private final String state;
    private final String country;

    public CityResponse(City city) {
        this.id = city.getId();
        this.name = city.getName();
        this.state = city.getState();
        this.country = city.getCountry();
    }
}