package com.example.seat_service.dto.seat;

import com.example.seat_service.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatRequest {
    private Long venue_id;
    private String section;
    private String rowLabel;
    private String seatLabel;
    private SeatType seatType;
    private BigDecimal basePrice;
}
