package com.example.seat_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "event_seat_status",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_seat",
                        columnNames = {"event_id", "seat_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ess_event", columnList = "event_id"),
                @Index(name = "idx_ess_event_status", columnList = "event_id,status"),
                @Index(name = "idx_ess_status_lockedAt", columnList = "status,locked_at"),
                @Index(name = "idx_ess_locked_by", columnList = "locked_by_user_id"),
                @Index(name = "idx_ess_booked_by", columnList = "booked_by_user_id"),
                @Index(name = "idx_ess_event_seat", columnList = "event_id,seat_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSeatStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_seat_status_seq")
    @SequenceGenerator(
            name = "event_seat_status_seq",
            sequenceName = "event_seat_status_seq",
            allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatBookingStatus status;

    @Column(name = "locked_by_user_id")
    private String lockedByUserId;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @Column(name = "booked_by_user_id")
    private String bookedByUserId;

    @Column(name = "booked_at")
    private Instant bookedAt;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Version
    @Column(nullable = false)
    private Integer version;
}