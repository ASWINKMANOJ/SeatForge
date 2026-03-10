package com.example.seat_service.dto.booking;

import com.example.seat_service.dto.bookingSeat.BookingSeatResponse;
import com.example.seat_service.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long eventId;
    private String eventTitle;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private Instant bookedAt;
    private Instant cancelledAt;
    private List<BookingSeatResponse> seats;
}