package com.example.seat_service.repository;

import com.example.seat_service.entity.Booking;
import com.example.seat_service.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // idx_booking_code — booking confirmation lookup
    @Query("SELECT b FROM Booking b JOIN FETCH b.event WHERE b.bookingCode = :bookingCode")
    Optional<Booking> findByBookingCode(@Param("bookingCode") String bookingCode);

    // idx_booking_user — all bookings for a user
    @Query("SELECT b FROM Booking b JOIN FETCH b.event WHERE b.userId = :userId")
    List<Booking> findAllByUserId(@Param("userId") String userId);

    // idx_booking_user_event — check if user already booked this event
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.event.id = :eventId AND b.status = :status")
    Optional<Booking> findByUserIdAndEventIdAndStatus(
            @Param("userId") String userId,
            @Param("eventId") Long eventId,
            @Param("status") BookingStatus status);
}
