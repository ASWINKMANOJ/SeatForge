package com.example.seat_service.dto.EventSeatStatus;

import com.example.seat_service.entity.SeatBookingStatus;
import com.example.seat_service.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSeatStatusResponse {
    private Long id;
    private Long seatId;
    private String section;
    private String rowLabel;
    private String seatLabel;
    private SeatType seatType;
    private SeatBookingStatus status;
    private BigDecimal price;
}
