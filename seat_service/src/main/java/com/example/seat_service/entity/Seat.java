package com.example.seat_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(
        name = "seat",
        indexes = {
                @Index(name = "idx_seat_venue", columnList = "venue_id"),
                @Index(name = "idx_seat_venue_section_row", columnList = "venue_id,section,row_label"),
                @Index(name = "idx_seat_label_unique", columnList = "venue_id,section,row_label,seat_label", unique = true)
        }
)
@Data
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_seq")
    @SequenceGenerator(
            name = "seat_seq",
            sequenceName = "seat_seq",
            allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    private String section;

    @Column(name = "row_label")
    private String rowLabel;

    @Column(name = "seat_label")
    private String seatLabel;

    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    private BigDecimal basePrice;
}