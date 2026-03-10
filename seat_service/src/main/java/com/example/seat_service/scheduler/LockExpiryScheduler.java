package com.example.seat_service.scheduler;

import com.example.seat_service.entity.EventSeatStatus;
import com.example.seat_service.entity.SeatBookingStatus;
import com.example.seat_service.repository.EventSeatStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// LockExpiryScheduler.java
@Slf4j
@Component
@RequiredArgsConstructor
public class LockExpiryScheduler {

    private final EventSeatStatusRepository eventSeatStatusRepository;

    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    @Transactional
    public void expireLockedSeats() {
        Instant expiry = Instant.now().minus(10, ChronoUnit.MINUTES);

        List<EventSeatStatus> expiredSeats = eventSeatStatusRepository
                .findAllLockedBefore(SeatBookingStatus.LOCKED, expiry);

        if (expiredSeats.isEmpty()) {
            return;
        }

        expiredSeats.forEach(s -> {
            s.setStatus(SeatBookingStatus.AVAILABLE);
            s.setLockedByUserId(null);
            s.setLockedAt(null);
        });

        eventSeatStatusRepository.saveAll(expiredSeats);

        log.info("Expired {} locked seats", expiredSeats.size());
    }
}