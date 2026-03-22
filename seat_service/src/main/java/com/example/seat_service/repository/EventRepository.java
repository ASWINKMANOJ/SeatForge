package com.example.seat_service.repository;

import com.example.seat_service.entity.Event;
import com.example.seat_service.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e JOIN FETCH e.venue WHERE e.venue.id = :venueId")
    List<Event> findAllByVenueId(@Param("venueId") Long venueId);

    // idx_event_start
    @Query("SELECT e FROM Event e JOIN FETCH e.venue WHERE e.startTime >= :from AND e.startTime <= :to")
    List<Event> findAllByStartTimeBetween(@Param("from") Instant from, @Param("to") Instant to);

    // idx_event_status — already done
    @Query("SELECT e FROM Event e JOIN FETCH e.venue WHERE e.status = :status")
    List<Event> findByStatus(@Param("status") EventStatus status);

    // idx_event_venue_start — composite
    @Query("SELECT e FROM Event e JOIN FETCH e.venue WHERE e.venue.id = :venueId AND e.startTime >= :from AND e.startTime <= :to")
    List<Event> findAllByVenueIdAndStartTimeBetween(@Param("venueId") Long venueId, @Param("from") Instant from,
            @Param("to") Instant to);

    // idx_event_booking_window
    @Query("SELECT e FROM Event e JOIN FETCH e.venue WHERE e.bookingOpenAt <= :now AND e.bookingCloseAt >= :now")
    List<Event> findAllCurrentlyBookable(@Param("now") Instant now);

    // custom search
    @Query("SELECT e FROM Event e JOIN FETCH e.venue v WHERE " +
            "(LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
            +
            "AND LOWER(v.city.name) = LOWER(:city) AND e.status = :status")
    List<Event> searchEvents(@Param("query") String query, @Param("city") String city,
            @Param("status") EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.status = :status AND e.bookingOpenAt <= :now")
    List<Event> findByStatusAndBookingOpenAtBefore(@Param("status") EventStatus status, @Param("now") Instant now);

}
