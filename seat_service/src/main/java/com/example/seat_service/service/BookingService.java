package com.example.seat_service.service;

import com.example.seat_service.dto.booking.BookingRequest;
import com.example.seat_service.dto.booking.BookingResponse;
import com.example.seat_service.dto.booking.BookingSummaryResponse;
import com.example.seat_service.dto.booking.BookingDetailResponse;
import com.example.seat_service.dto.lock.LockRequest;
import com.example.seat_service.dto.lock.LockResponse;
import com.example.seat_service.entity.*;
import com.example.seat_service.repository.BookingRepository;
import com.example.seat_service.repository.BookingSeatRepository;
import com.example.seat_service.repository.EventRepository;
import com.example.seat_service.repository.EventSeatStatusRepository;
import com.example.seat_service.service.mapper.BookingMapper;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

// BookingService.java
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final EventSeatStatusRepository eventSeatStatusRepository;
    private final EventRepository eventRepository;
    private final BookingMapper bookingMapper;
    private final CacheManager cacheManager;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seatMap", key = "#request.eventId"),
            @CacheEvict(value = "events", key = "#request.eventId")
    })
    public LockResponse lockSeats(LockRequest request, String userId) {
        List<EventSeatStatus> seats = eventSeatStatusRepository
                .findAllByEventIdAndSeatIds(request.getEventId(), request.getSeatIds());

        if (seats.size() != request.getSeatIds().size()) {
            throw new EntityNotFoundException("One or more seats not found for this event");
        }

        List<String> unavailable = seats.stream()
                .filter(s -> s.getStatus() != SeatBookingStatus.AVAILABLE)
                .map(s -> s.getSeat().getRowLabel() + s.getSeat().getSeatLabel())
                .toList();

        if (!unavailable.isEmpty()) {
            throw new IllegalStateException("Seats no longer available: " + unavailable);
        }

        try {
            Instant now = Instant.now();
            seats.forEach(s -> {
                s.setStatus(SeatBookingStatus.LOCKED);
                s.setLockedByUserId(userId);
                s.setLockedAt(now);
            });

            eventSeatStatusRepository.saveAll(seats);

            log.info("Seats locked - eventId:{} userId:{} seats:{}",
                    request.getEventId(), userId, request.getSeatIds());

            checkAndUpdateEventStatus(request.getEventId());

            return LockResponse.builder()
                    .eventSeatStatusIds(seats.stream().map(EventSeatStatus::getId).toList())
                    .lockExpiresAt(now.plus(10, ChronoUnit.MINUTES))
                    .build();

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic lock conflict - eventId:{} userId:{}", request.getEventId(), userId);
            throw new IllegalStateException("Seat was just taken by another user, please select a different seat");
        }
    }

    @Transactional
    public void unlockSeats(List<Long> eventSeatStatusIds) {
        List<EventSeatStatus> seats = eventSeatStatusRepository.findAllById(eventSeatStatusIds);

        if (seats.isEmpty()) {
            throw new EntityNotFoundException("No seats found for given ids");
        }

        // get eventId from already fetched seats — no extra DB query
        Long eventId = seats.getFirst().getEvent().getId();

        seats.forEach(s -> {
            s.setStatus(SeatBookingStatus.AVAILABLE);
            s.setLockedByUserId(null);
            s.setLockedAt(null);
        });

        eventSeatStatusRepository.saveAll(seats);

        // manual eviction since eventId comes from fetched entity
        evictSeatCache(eventId);

        checkAndUpdateEventStatus(eventId);

        log.info("Seats unlocked - eventId:{} eventSeatStatusIds:{}", eventId, eventSeatStatusIds);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seatMap", key = "#request.eventId"),
            @CacheEvict(value = "events", key = "#request.eventId"),
            @CacheEvict(value = "userBookings", key = "#userId")
    })
    public BookingResponse createBooking(BookingRequest request, String userId) {
        bookingRepository.findByUserIdAndEventIdAndStatus(userId, request.getEventId(), BookingStatus.CONFIRMED)
                .ifPresent(b -> {
                    throw new IllegalStateException("User already has a confirmed booking for this event");
                });

        List<EventSeatStatus> seats = eventSeatStatusRepository
                .findAllByEventIdAndSeatIds(request.getEventId(), request.getSeatIds());

        boolean allLockedByUser = seats.stream()
                .allMatch(s -> s.getStatus() == SeatBookingStatus.LOCKED
                        && userId.equals(s.getLockedByUserId()));

        if (!allLockedByUser) {
            throw new IllegalStateException("Seats are no longer locked for this user, please select seats again");
        }

        if (request.getPaymentId() == null || request.getPaymentId().isBlank()) {
            throw new IllegalStateException("Payment not completed");
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + request.getEventId()));

        BigDecimal totalPrice = seats.stream()
                .map(EventSeatStatus::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant now = Instant.now();
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setEvent(event);
        booking.setBookingCode(generateBookingCode());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookedAt(now);
        booking.setPaymentId(request.getPaymentId());

        Booking saved = bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = seats.stream()
                .map(s -> BookingSeat.builder()
                        .booking(saved)
                        .seat(s.getSeat())
                        .price(s.getPrice())
                        .build())
                .toList();

        bookingSeatRepository.saveAll(bookingSeats);

        seats.forEach(s -> {
            s.setStatus(SeatBookingStatus.BOOKED);
            s.setBookedByUserId(userId);
            s.setBookedAt(now);
            s.setLockedByUserId(null);
            s.setLockedAt(null);
        });

        eventSeatStatusRepository.saveAll(seats);

        log.info("Booking created - bookingCode:{} userId:{} eventId:{}",
                saved.getBookingCode(), userId, request.getEventId());

        return bookingMapper.toResponse(saved, bookingSeats);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userBookings", key = "#userId"),
            @CacheEvict(value = "bookingDetail", key = "#bookingId")
    })
    public BookingResponse cancelBooking(Long bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("Booking does not belong to this user");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        // get eventId from already fetched booking — no extra DB query
        Long eventId = booking.getEvent().getId();

        List<BookingSeat> bookingSeats = bookingSeatRepository.findAllByBookingId(bookingId);

        List<Long> seatIds = bookingSeats.stream()
                .map(bs -> bs.getSeat().getId())
                .toList();

        List<EventSeatStatus> eventSeats = eventSeatStatusRepository
                .findAllByEventIdAndSeatIds(eventId, seatIds);

        eventSeats.forEach(s -> {
            s.setStatus(SeatBookingStatus.AVAILABLE);
            s.setBookedByUserId(null);
            s.setBookedAt(null);
        });

        eventSeatStatusRepository.saveAll(eventSeats);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        Booking saved = bookingRepository.save(booking);

        // manual eviction since eventId comes from fetched entity
        evictSeatCache(eventId);

        checkAndUpdateEventStatus(eventId);

        log.info("Booking cancelled - bookingId:{} userId:{} eventId:{}", bookingId, userId, eventId);

        return bookingMapper.toResponse(saved, bookingSeats);
    }

    @Cacheable(value = "bookingDetail", key = "#bookingId")
    public BookingDetailResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        List<BookingSeat> bookingSeats = bookingSeatRepository.findAllByBookingId(bookingId);
        return bookingMapper.toDetailResponse(booking, bookingSeats);
    }

    @Cacheable(value = "userBookings", key = "#userId")
    public List<BookingSummaryResponse> getBookingsByUser(String userId) {
        return bookingRepository.findAllByUserId(userId)
                .stream()
                .map(booking -> {
                    int seatCount = bookingSeatRepository.countByBookingId(booking.getId()).intValue();
                    return bookingMapper.toSummaryResponse(booking, seatCount);
                })
                .toList();
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void checkAndUpdateEventStatus(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null || (event.getStatus() != EventStatus.ACTIVE && event.getStatus() != EventStatus.SOLD_OUT)) {
            return;
        }
        long availableSeats = eventSeatStatusRepository.countByEventIdAndStatus(eventId, SeatBookingStatus.AVAILABLE);
        boolean changed = false;
        if (availableSeats == 0 && event.getStatus() == EventStatus.ACTIVE) {
            event.setStatus(EventStatus.SOLD_OUT);
            changed = true;
        } else if (availableSeats > 0 && event.getStatus() == EventStatus.SOLD_OUT) {
            event.setStatus(EventStatus.ACTIVE);
            changed = true;
        }

        if (changed) {
            event.setUpdatedAt(Instant.now());
            eventRepository.save(event);
            evictEventCache(eventId);
            log.info("Event {} status automatically updated to {}", eventId, event.getStatus());
        }
    }

    private void evictEventCache(Long eventId) {
        Cache events = cacheManager.getCache("events");
        Cache eventsAdmin = cacheManager.getCache("eventsAdmin");
        Cache eventDetail = cacheManager.getCache("eventDetail");
        if (events != null) events.clear();
        if (eventsAdmin != null) eventsAdmin.clear();
        if (eventDetail != null) eventDetail.evict(eventId);
    }

    private void evictSeatCache(Long eventId) {
        Cache seatMap = cacheManager.getCache("seatMap");
        Cache events = cacheManager.getCache("events");
        if (seatMap != null)
            seatMap.evict(eventId);
        if (events != null)
            events.evict(eventId);
    }

    private String generateBookingCode() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
