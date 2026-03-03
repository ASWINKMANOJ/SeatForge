package com.example.seat_service.service;

import com.example.seat_service.dto.seat.SeatRequest;
import com.example.seat_service.dto.seat.SeatResponse;
import com.example.seat_service.entity.Seat;
import com.example.seat_service.entity.Venue;
import com.example.seat_service.repository.SeatRepository;
import com.example.seat_service.repository.VenueRepository;
import com.example.seat_service.service.mapper.SeatMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;
    private final SeatMapper seatMapper;

    public List<SeatResponse> findAllSeatByVenue(Long venueId) {
        List<Seat> seats = seatRepository.findAllWithVenue(venueId);

        if (seats.isEmpty()) {
            // checking why it's empty
            if (!venueRepository.existsById(venueId)) {
                throw new EntityNotFoundException("Venue not found with id: " + venueId);
            }
            return List.of(); // venue exists but has no seats
        }

        return seats.stream()
                .map(seatMapper::toSeatResponse)
                .toList();
    }

    public SeatResponse findSeatById(Long seatId) {
        return seatMapper.toSeatResponse(seatRepository.findById(seatId).orElseThrow(()-> new EntityNotFoundException("Seat not found with id: " + seatId)));
    }

    public SeatResponse createSeat(SeatRequest seatRequest) {
        Venue venue = venueRepository.findById(seatRequest.getVenue_id())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + seatRequest.getVenue_id()));

        if (seatRepository.existsByRowLabelAndSeatLabelAndVenue_Id(
                seatRequest.getRowLabel(),
                seatRequest.getSeatLabel(),
                seatRequest.getVenue_id())) {
            throw new IllegalStateException("Seat " + seatRequest.getRowLabel() + seatRequest.getSeatLabel()
                    + " already exists in venue id: " + seatRequest.getVenue_id());
        }

        Seat seat = seatMapper.toEntity(seatRequest, venue);
        return seatMapper.toSeatResponse(seatRepository.save(seat));
    }

    public SeatResponse updateSeat(Long seatId, SeatRequest seatRequest) {
        Seat existing = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found with id: " + seatId));

        // update venue only if venueId changed
        if (!existing.getVenue().getId().equals(seatRequest.getVenue_id())) {
            Venue newVenue = venueRepository.findById(seatRequest.getVenue_id())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + seatRequest.getVenue_id()));
            existing.setVenue(newVenue);
        }

        // duplicate check — exclude current seat from check
        if (seatRepository.existsByRowLabelAndSeatLabelAndVenue_IdAndIdNot(
                seatRequest.getRowLabel(),
                seatRequest.getSeatLabel(),
                seatRequest.getVenue_id(),
                seatId)) {
            throw new IllegalStateException("Seat " + seatRequest.getRowLabel() + seatRequest.getSeatLabel()
                    + " already exists in venue id: " + seatRequest.getVenue_id());
        }

        existing.setRowLabel(seatRequest.getRowLabel());
        existing.setSeatLabel(seatRequest.getSeatLabel());
        existing.setSeatType(seatRequest.getSeatType());

        return seatMapper.toSeatResponse(seatRepository.save(existing));
    }

    public void deleteSeat(Long seatId) {
        if (!seatRepository.existsById(seatId)) {
            throw new EntityNotFoundException("Seat not found with id: " + seatId);
        }
        seatRepository.deleteById(seatId);
    }
}
