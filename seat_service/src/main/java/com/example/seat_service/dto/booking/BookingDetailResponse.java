package com.example.seat_service.dto.booking;

import com.example.seat_service.dto.bookingSeat.BookingSeatResponse;
import com.example.seat_service.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse implements Serializable {
    private Long id;
    private String bookingCode;
    private Long eventId;
    private String eventTitle;
    private String eventImageUrl;
    private Instant eventStartTime;
    private Instant eventEndTime;
    private String venueName;
    private String venueAddress;
    private String cityName;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private Instant bookedAt;
    private Instant cancelledAt;
    private String paymentId;
    private List<BookingSeatResponse> seats;
}
