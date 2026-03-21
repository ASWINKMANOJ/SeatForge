package com.example.seat_service.service;

import com.example.seat_service.dto.admin.AdminStatsResponse;
import com.example.seat_service.dto.venue.VenueCapacityResponse;
import com.example.seat_service.entity.SeatBookingStatus;
import com.example.seat_service.repository.BookingRepository;
import com.example.seat_service.repository.EventSeatStatusRepository;
import com.example.seat_service.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.seat_service.entity.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

// service/AdminService.java
@Service
@RequiredArgsConstructor
public class AdminService {

    private final EventSeatStatusRepository eventSeatStatusRepository;
    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;

    public AdminStatsResponse getStats() {
        Instant startOfDay = LocalDate.now()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        List<VenueCapacityResponse> venueCapacities = venueRepository
                .findTopVenuesByCapacity()
                .stream()
                .map(venue -> {
                    Long booked = eventSeatStatusRepository
                            .countByStatus(SeatBookingStatus.BOOKED);
                    int percentage = venue.getTotalCapacity() > 0
                            ? (int) (booked * 100 / venue.getTotalCapacity())
                            : 0;
                    return VenueCapacityResponse.builder()
                            .venueName(venue.getName())
                            .totalCapacity(venue.getTotalCapacity())
                            .bookedSeats(booked)
                            .occupancyPercentage(Math.min(percentage, 100))
                            .build();
                })
                .toList();

        return AdminStatsResponse.builder()
                .activeLocksCount(eventSeatStatusRepository
                        .countByStatus(SeatBookingStatus.LOCKED))
                .bookingsTodayCount(bookingRepository
                        .countByBookedAtAfterAndStatus(startOfDay, BookingStatus.CONFIRMED))
                .currentRevenue(bookingRepository
                        .sumTotalPriceByStatus(BookingStatus.CONFIRMED))
                .venueCapacities(venueCapacities)
                .build();
    }
}
