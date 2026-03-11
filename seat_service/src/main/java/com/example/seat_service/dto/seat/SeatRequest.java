package com.example.seat_service.dto.seat;

import com.example.seat_service.entity.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatRequest {
    @NotNull(message = "Venue id is required")
    private Long venue_id;
    @NotBlank(message = "Section is required")
    private String section;
    @NotBlank(message = "Row label is required")
    private String rowLabel;
    @NotBlank(message = "Seat label is required")
    private String seatLabel;
    @NotNull(message = "Seat type is required")
    private SeatType seatType;
    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;
}
