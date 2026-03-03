package com.example.seat_service.service.mapper;


import com.example.seat_service.dto.seat.SeatRequest;
import com.example.seat_service.dto.seat.SeatResponse;
import com.example.seat_service.entity.Seat;
import com.example.seat_service.entity.Venue;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .venue_id(seat.getVenue().getId())
                .section(seat.getSection())
                .rowLabel(seat.getRowLabel())
                .seatLabel(seat.getSeatLabel())
                .seatType(seat.getSeatType())
                .basePrice(seat.getBasePrice())
                .build();
    }

    public Seat toEntity(SeatRequest request, Venue venue) {
        Seat seat = new Seat();
        seat.setVenue(venue);
        seat.setSection(request.getSection());
        seat.setRowLabel(request.getRowLabel());
        seat.setSeatType(request.getSeatType());
        seat.setSeatLabel(request.getSeatLabel());
        seat.setBasePrice(request.getBasePrice());
        return seat;
    }
}
