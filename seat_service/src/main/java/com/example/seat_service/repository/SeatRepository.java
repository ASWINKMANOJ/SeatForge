package com.example.seat_service.repository;

import com.example.seat_service.entity.Seat;
import com.example.seat_service.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s JOIN FETCH s.venue WHERE s.venue.id = :venueId")
    List<Seat> findAllWithVenue(@Param("venueId") Long venueId);

    boolean existsByRowLabelAndSeatLabelAndVenue_Id(String rowLabel, String seatLabel, Long venueId);

    boolean existsByRowLabelAndSeatLabelAndVenue_IdAndIdNot(String rowLabel, String seatLabel, Long venueId, Long seatId);
}
