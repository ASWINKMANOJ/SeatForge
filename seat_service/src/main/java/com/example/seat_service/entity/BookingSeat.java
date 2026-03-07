package com.example.seat_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(
        name = "booking_seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_seat",
                        columnNames = {"booking_id", "seat_id"}
                )
        },
        indexes = {
                @Index(name = "idx_bs_booking_seat", columnList = "booking_id,seat_id")
        }
)
@Data
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seat_seq")
    @SequenceGenerator(
            name = "booking_seat_seq",
            sequenceName = "booking_seat_seq",
            allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    private BigDecimal price;
}