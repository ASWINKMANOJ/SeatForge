package com.example.seat_service.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(
        name = "venue",
        indexes = {
                @Index(name = "idx_venue_city", columnList = "city_id"),
                @Index(name = "idx_venue_active", columnList = "active"),
                @Index(name = "idx_venue_city_active", columnList = "city_id,active")
        }
)
@Data
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venue_seq")
    @SequenceGenerator(
            name = "venue_seq",
            sequenceName = "venue_seq",
            allocationSize = 50
    )
    private Long id;
    @Column(nullable = false)
    private String name;
    private String address;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id")
    private City city;
    @Enumerated(EnumType.STRING)
    private VenueType type;
    private Integer totalCapacity;
    private String imageUrl;
    private Boolean active;
    private Instant createdAt;
}
