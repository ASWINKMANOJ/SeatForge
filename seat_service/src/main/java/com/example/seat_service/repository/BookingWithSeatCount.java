package com.example.seat_service.repository;



import org.springframework.stereotype.Repository;
import com.example.seat_service.entity.Booking;

@Repository
public interface BookingWithSeatCount {
    Booking getBooking();
    Integer getSeatCount();
}
