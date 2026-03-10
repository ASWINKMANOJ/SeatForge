package com.example.seat_service.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Long eventId;
    private List<Long> seatIds;
    private String paymentId;   // dummy payment id
}