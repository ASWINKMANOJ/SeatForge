package com.example.seat_service.service;

import com.example.seat_service.dto.booking.BookingRequest;
import com.example.seat_service.dto.booking.BookingResponse;
import com.example.seat_service.dto.lock.LockRequest;
import com.example.seat_service.dto.lock.LockResponse;
import com.example.seat_service.entity.*;
import com.example.seat_service.repository.BookingRepository;
import com.example.seat_service.repository.BookingSeatRepository;
import com.example.seat_service.repository.EventRepository;
import com.example.seat_service.repository.EventSeatStatusRepository;
import com.example.seat_service.service.mapper.BookingMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

// BookingService.java
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final EventSeatStatusRepository eventSeatStatusRepository;
    private final EventRepository eventRepository;
    private final BookingMapper bookingMapper;

    // LOCK SEATS
    @Transactional
    public LockResponse lockSeats(LockRequest request, String userId) {
        List<EventSeatStatus> seats = eventSeatStatusRepository
                .findAllByEventIdAndSeatIds(request.getEventId(), request.getSeatIds());

        // check requested seats match found seats
        if (seats.size() != request.getSeatIds().size()) {
            throw new EntityNotFoundException("One or more seats not found for this event");
        }

        // check all seats are AVAILABLE
        List<String> unavailable = seats.stream()
                .filter(s -> s.getStatus() != SeatBookingStatus.AVAILABLE)
                .map(s -> s.getSeat().getRowLabel() + s.getSeat().getSeatLabel())
                .toList();

        if (!unavailable.isEmpty()) {
            throw new IllegalStateException("Seats no longer available: " + unavailable);
        }

        // lock seats
        Instant now = Instant.now();
        seats.forEach(s -> {
            s.setStatus(SeatBookingStatus.LOCKED);
            s.setLockedByUserId(userId);
            s.setLockedAt(now);
        });

        eventSeatStatusRepository.saveAll(seats);

        return LockResponse.builder()
                .eventSeatStatusIds(seats.stream().map(EventSeatStatus::getId).toList())
                .lockExpiresAt(now.plus(10, ChronoUnit.MINUTES))
                .build();
    }

    // UNLOCK SEATS
    @Transactional
    public void unlockSeats(List<Long> eventSeatStatusIds) {
        List<EventSeatStatus> seats = eventSeatStatusRepository.findAllById(eventSeatStatusIds);

        seats.forEach(s -> {
            s.setStatus(SeatBookingStatus.AVAILABLE);
            s.setLockedByUserId(null);
            s.setLockedAt(null);
        });

        eventSeatStatusRepository.saveAll(seats);
    }

    // CREATE BOOKING
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String userId) {
        // check user hasn't already booked this event
        bookingRepository.findByUserIdAndEventIdAndStatus(userId, request.getEventId(), BookingStatus.CONFIRMED)
                .ifPresent(b -> {
                    throw new IllegalStateException("User already has a confirmed booking for this event");
                });

        // verify seats are still locked by this user
        List<EventSeatStatus> seats = eventSeatStatusRepository
                .findAllByEventIdAndSeatIds(request.getEventId(), request.getSeatIds());

        boolean allLockedByUser = seats.stream()
                .allMatch(s -> s.getStatus() == SeatBookingStatus.LOCKED
                        && userId.equals(s.getLockedByUserId()));

        if (!allLockedByUser) {
            throw new IllegalStateException("Seats are no longer locked for this user, please select seats again");
        }

        // dummy payment check
        if (request.getPaymentId() == null || request.getPaymentId().isBlank()) {
            throw new IllegalStateException("Payment not completed");
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + request.getEventId()));

        // calculate total price from locked seats
        BigDecimal totalPrice = seats.stream()
                .map(EventSeatStatus::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // create booking
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

        // create BookingSeat rows
        List<BookingSeat> bookingSeats = seats.stream()
                .map(s -> BookingSeat.builder()
                        .booking(saved)
                        .seat(s.getSeat())
                        .price(s.getPrice())
                        .build())
                .toList();

        bookingSeatRepository.saveAll(bookingSeats);

        // mark seats as BOOKED
        seats.forEach(s -> {
            s.setStatus(SeatBookingStatus.BOOKED);
            s.setBookedByUserId(userId);
            s.setBookedAt(now);
            s.setLockedByUserId(null);
            s.setLockedAt(null);
        });

        eventSeatStatusRepository.saveAll(seats);

        return bookingMapper.toResponse(saved, bookingSeats);
    }

    // CANCEL BOOKING
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // verify booking belongs to user
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("Booking does not belong to this user");
        }

        // verify booking is CONFIRMED
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        // fetch booking seats before deletion
        List<BookingSeat> bookingSeats = bookingSeatRepository.findAllByBookingId(bookingId);

        // release seats back to AVAILABLE
        List<Long> seatIds = bookingSeats.stream()
                .map(bs -> bs.getSeat().getId())
                .toList();

        List<EventSeatStatus> eventSeats = eventSeatStatusRepository
                .findAllByEventIdAndSeatIds(booking.getEvent().getId(), seatIds);

        eventSeats.forEach(s -> {
            s.setStatus(SeatBookingStatus.AVAILABLE);
            s.setBookedByUserId(null);
            s.setBookedAt(null);
        });

        eventSeatStatusRepository.saveAll(eventSeats);

        // cancel booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        Booking saved = bookingRepository.save(booking);

        return bookingMapper.toResponse(saved, bookingSeats);
    }

    // GET BOOKING BY ID
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        List<BookingSeat> bookingSeats = bookingSeatRepository.findAllByBookingId(bookingId);
        return bookingMapper.toResponse(booking, bookingSeats);
    }

    // GET BOOKINGS BY USER
    public List<BookingResponse> getBookingsByUser(String userId) {
        return bookingRepository.findAllByUserId(userId)
                .stream()
                .map(booking -> bookingMapper.toResponse(
                        booking,
                        bookingSeatRepository.findAllByBookingId(booking.getId())))
                .toList();
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private String generateBookingCode() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}