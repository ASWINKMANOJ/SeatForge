package com.example.seat_service.dto.city;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityRequest {
    @NotBlank(message = "City name is required")
    String name;
    @NotBlank(message = "State name is required")
    String state;
    @NotBlank(message = "Country name is required")
    String country;
}
