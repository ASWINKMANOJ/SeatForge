package com.example.seat_service.dto.seat;

import com.example.seat_service.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse implements Serializable {
    private Long id;
    private Long venue_id;
    private String section;
    private String rowLabel;
    private String seatLabel;
    private SeatType seatType;
    private BigDecimal basePrice;
}
