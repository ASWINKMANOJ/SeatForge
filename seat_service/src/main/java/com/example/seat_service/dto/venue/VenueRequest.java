package com.example.seat_service.dto.venue;

import com.example.seat_service.entity.VenueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueRequest {
    @NotBlank(message = "Venue name is required")
    private String name;
    private String address;
    @NotNull(message = "City id is required")
    private Long city_id;
    @NotNull(message = "Venue type is required")
    private VenueType venue_type;
    @NotNull(message = "Total capacity is required")
    private Integer totalCapacity;
    @NotNull(message = "Active status is required")
    private Boolean active;
    @NotNull(message = "Image url is required")
    private String imageUrl;
}
