package com.example.seat_service.service.mapper;

import com.example.seat_service.dto.EventSeatStatus.EventSeatStatusResponse;
import com.example.seat_service.entity.EventSeatStatus;
import com.example.seat_service.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class EventSeatStatusMapper {

    public EventSeatStatusResponse toResponse(EventSeatStatus eventSeatStatus) {
        Seat seat = eventSeatStatus.getSeat();
        return EventSeatStatusResponse.builder()
                .id(eventSeatStatus.getId())
                .seatId(seat.getId())
                .section(seat.getSection())
                .rowLabel(seat.getRowLabel())
                .seatLabel(seat.getSeatLabel())
                .seatType(seat.getSeatType())
                .status(eventSeatStatus.getStatus())
                .price(eventSeatStatus.getPrice())
                .build();
    }
}