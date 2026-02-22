package com.example.seat_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "booking",
        indexes = {
                @Index(name = "idx_booking_user", columnList = "user_id"),
                @Index(name = "idx_booking_event", columnList = "event_id"),
                @Index(name = "idx_booking_code", columnList = "booking_code", unique = true),
                @Index(name = "idx_booking_status", columnList = "status"),
                @Index(name = "idx_booking_user_event", columnList = "user_id,event_id")
        }
)
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
    @SequenceGenerator(
            name = "booking_seq",
            sequenceName = "booking_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "booked_by_user_id")
    private String user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false, unique = true)
    private String bookingCode;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Instant bookedAt;

    private Instant cancelledAt;

    private String paymentId;
}