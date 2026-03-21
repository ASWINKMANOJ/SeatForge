package com.example.seat_service.service.mapper;

import com.example.seat_service.dto.booking.BookingResponse;
import com.example.seat_service.dto.booking.BookingSummaryResponse;
import com.example.seat_service.dto.booking.BookingDetailResponse;
import com.example.seat_service.dto.bookingSeat.BookingSeatResponse;
import com.example.seat_service.entity.Booking;
import com.example.seat_service.entity.BookingSeat;
import com.example.seat_service.entity.Seat;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking, List<BookingSeat> bookingSeats) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .cancelledAt(booking.getCancelledAt())
                .seats(bookingSeats.stream()
                        .map(this::toBookingSeatResponse)
                        .toList())
                .build();
    }

    private BookingSeatResponse toBookingSeatResponse(BookingSeat bookingSeat) {
        Seat seat = bookingSeat.getSeat();
        return BookingSeatResponse.builder()
                .id(bookingSeat.getId())
                .seatId(seat.getId())
                .section(seat.getSection())
                .rowLabel(seat.getRowLabel())
                .seatLabel(seat.getSeatLabel())
                .seatType(seat.getSeatType())
                .price(bookingSeat.getPrice())
                .build();
    }

    public BookingSummaryResponse toSummaryResponse(Booking booking, int seatCount) {
        return BookingSummaryResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .eventImageUrl(booking.getEvent().getImageUrl())
                .eventStartTime(booking.getEvent().getStartTime())
                .venueName(booking.getEvent().getVenue().getName())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .cancelledAt(booking.getCancelledAt())
                .seatCount(seatCount)
                .build();
    }

    public BookingDetailResponse toDetailResponse(Booking booking, List<BookingSeat> seats) {
        return BookingDetailResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .eventImageUrl(booking.getEvent().getImageUrl())
                .eventStartTime(booking.getEvent().getStartTime())
                .eventEndTime(booking.getEvent().getEndTime())
                .venueName(booking.getEvent().getVenue().getName())
                .venueAddress(booking.getEvent().getVenue().getAddress())
                .cityName(booking.getEvent().getVenue().getCity().getName())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .cancelledAt(booking.getCancelledAt())
                .paymentId(booking.getPaymentId())
                .seats(seats.stream().map(this::toBookingSeatResponse).toList())
                .build();
    }
}