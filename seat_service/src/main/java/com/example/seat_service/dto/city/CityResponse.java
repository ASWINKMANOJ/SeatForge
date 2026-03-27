package com.example.seat_service.dto.city;

import com.example.seat_service.entity.City;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityResponse implements Serializable {
    private  Long id;
    private  String name;
    private  String state;
    private  String country;
    private  String imageUrl;
    private  String timeZone;
}