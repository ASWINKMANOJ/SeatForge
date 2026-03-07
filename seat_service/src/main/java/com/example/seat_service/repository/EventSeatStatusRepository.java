package com.example.seat_service.repository;

import com.example.seat_service.entity.Event;
import com.example.seat_service.entity.EventSeatStatus;
import com.example.seat_service.entity.SeatBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

// EventSeatStatusRepository.java
@Repository
public interface EventSeatStatusRepository extends JpaRepository<EventSeatStatus, Long> {

    // seat availability page — returns all seats with status for frontend seat map
    @Query("SELECT e FROM EventSeatStatus e JOIN FETCH e.seat WHERE e.event.id = :eventId")
    List<EventSeatStatus> findAllByEventId(@Param("eventId") Long eventId);

    // filter by status — AVAILABLE / LOCKED / BOOKED
    @Query("SELECT e FROM EventSeatStatus e JOIN FETCH e.seat WHERE e.event.id = :eventId AND e.status = :status")
    List<EventSeatStatus> findAllByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") SeatBookingStatus status);

    // lock specific seats during checkout
    @Query("SELECT e FROM EventSeatStatus e JOIN FETCH e.seat WHERE e.event.id = :eventId AND e.seat.id IN :seatIds")
    List<EventSeatStatus> findAllByEventIdAndSeatIds(@Param("eventId") Long eventId, @Param("seatIds") List<Long> seatIds);

    // lock expiry scheduler — find all locked seats past expiry time
    @Query("SELECT e FROM EventSeatStatus e WHERE e.status = :status AND e.lockedAt < :expiry")
    List<EventSeatStatus> findAllLockedBefore(@Param("status") SeatBookingStatus status, @Param("expiry") Instant expiry);

    // available seat count for event card
    @Query("SELECT COUNT(e) FROM EventSeatStatus e WHERE e.event.id = :eventId AND e.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") SeatBookingStatus status);

    // batch count for event list — avoids N+1 on home page
    @Query("SELECT e.event.id, COUNT(e) FROM EventSeatStatus e WHERE e.event.id IN :eventIds AND e.status = :status GROUP BY e.event.id")
    List<Object[]> countAvailableSeatsForEvents(@Param("eventIds") List<Long> eventIds, @Param("status") SeatBookingStatus status);

    // delete all seats when event is deleted
    void deleteAllByEventId(Long eventId);
}