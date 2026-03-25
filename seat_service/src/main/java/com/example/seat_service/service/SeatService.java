package com.example.seat_service.service;

import com.example.seat_service.dto.seat.SeatBulkCreateRequest;
import com.example.seat_service.dto.seat.SeatRequest;
import com.example.seat_service.dto.seat.SeatResponse;
import com.example.seat_service.entity.Seat;
import com.example.seat_service.entity.Venue;
import com.example.seat_service.repository.SeatRepository;
import com.example.seat_service.repository.VenueRepository;
import com.example.seat_service.service.mapper.SeatMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;
    private final SeatMapper seatMapper;

    @Cacheable(value = "seats", key = "'venue:' + #venueId")
    public List<SeatResponse> findAllSeatByVenue(Long venueId) {
        log.info("Cache MISS - fetching seats by venueId:{} from DB", venueId);
        List<Seat> seats = seatRepository.findAllWithVenue(venueId);

        if (seats.isEmpty()) {
            if (!venueRepository.existsById(venueId)) {
                throw new EntityNotFoundException("Venue not found with id: " + venueId);
            }
            return List.of();
        }

        return seats.stream()
                .map(seatMapper::toSeatResponse)
                .toList();
    }

    @Cacheable(value = "seats", key = "#seatId")
    public SeatResponse findSeatById(Long seatId) {
        log.info("Cache MISS - fetching seat id:{} from DB", seatId);
        return seatMapper.toSeatResponse(seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found with id: " + seatId)));
    }

    @CacheEvict(value = "seats", allEntries = true)
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

    @CacheEvict(value = "seats", allEntries = true)
    public List<SeatResponse> createSeatBulk(SeatBulkCreateRequest request) {

        // 1️⃣ Fetch venue once
        Venue venue = venueRepository.findById(request.getVenue_id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Venue not found with id: " + request.getVenue_id()));

        // 2️⃣ Get existing seats for this venue
        List<Seat> existingSeats = seatRepository.findAllWithVenue(request.getVenue_id());

        // Build a set for fast lookup
        Set<String> existingSeatKeys = existingSeats.stream()
                .map(seat -> seat.getRowLabel() + "_" + seat.getSeatLabel())
                .collect(Collectors.toSet());

        List<Seat> seatsToSave = new ArrayList<>();

        // 3️⃣ Generate seats
        for (String section : request.getSections()) {
            for (String row : request.getRows()) {
                for (int i = 1; i <= request.getSeatsPerRow(); i++) {

                    String seatLabel = String.valueOf(i);
                    String key = row + "_" + seatLabel;

                    // Skip duplicates
                    if (existingSeatKeys.contains(key)) {
                        continue;
                    }

                    Seat seat = new Seat();
                    seat.setVenue(venue);
                    seat.setSection(section);
                    seat.setRowLabel(row);
                    seat.setSeatLabel(seatLabel);
                    seat.setSeatType(request.getSeatType());
                    seat.setBasePrice(request.getBasePrice());

                    seatsToSave.add(seat);
                }
            }
        }

        // 4️⃣ Save in batch
        List<Seat> savedSeats = seatRepository.saveAll(seatsToSave);

        // 5️⃣ Map response
        return savedSeats.stream()
                .map(seatMapper::toSeatResponse)
                .toList();
    }

    @CacheEvict(value = "seats", allEntries = true)
    public SeatResponse updateSeat(Long seatId, SeatRequest seatRequest) {
        Seat existing = seatRepository.findById(seatId)
                .orElseThrow(() -> new EntityNotFoundException("Seat not found with id: " + seatId));

        if (!existing.getVenue().getId().equals(seatRequest.getVenue_id())) {
            Venue newVenue = venueRepository.findById(seatRequest.getVenue_id())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + seatRequest.getVenue_id()));
            existing.setVenue(newVenue);
        }

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

    @CacheEvict(value = "seats", allEntries = true)
    public void deleteSeat(Long seatId) {
        if (!seatRepository.existsById(seatId)) {
            throw new EntityNotFoundException("Seat not found with id: " + seatId);
        }
        seatRepository.deleteById(seatId);
    }
}