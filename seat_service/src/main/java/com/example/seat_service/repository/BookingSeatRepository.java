package com.example.seat_service.repository;

import com.example.seat_service.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    // fetch all seats for a booking — used in BookingResponse
    @Query("SELECT bs FROM BookingSeat bs JOIN FETCH bs.seat WHERE bs.booking.id = :bookingId")
    List<BookingSeat> findAllByBookingId(@Param("bookingId") Long bookingId);

    // cleanup when booking is canceled
    void deleteAllByBookingId(Long bookingId);
}
