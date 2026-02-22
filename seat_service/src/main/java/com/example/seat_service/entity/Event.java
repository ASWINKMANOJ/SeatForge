package com.example.seat_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(
        name = "event",
        indexes = {
                @Index(name = "idx_event_venue", columnList = "venue_id"),
                @Index(name = "idx_event_start", columnList = "start_time"),
                @Index(name = "idx_event_status", columnList = "status"),
                @Index(name = "idx_event_venue_start", columnList = "venue_id,start_time"),
                @Index(name = "idx_event_booking_window", columnList = "booking_open_at,booking_close_at")
        }
)
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_seq")
    @SequenceGenerator(
            name = "event_seq",
            sequenceName = "event_seq",
            allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Instant startTime;

    private Instant endTime;

    private Instant bookingOpenAt;

    private Instant bookingCloseAt;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}