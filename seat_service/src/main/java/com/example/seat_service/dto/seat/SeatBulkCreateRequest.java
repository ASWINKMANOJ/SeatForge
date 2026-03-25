package com.example.seat_service.dto.seat;

import com.example.seat_service.entity.SeatType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatBulkCreateRequest {

    @NotNull
    private Long venue_id;

    @NotEmpty
    private List<String> sections;

    @NotEmpty
    private List<String> rows;

    @NotNull
    private Integer seatsPerRow;

    @NotNull
    private SeatType seatType;

    @NotNull
    private BigDecimal basePrice;
}
