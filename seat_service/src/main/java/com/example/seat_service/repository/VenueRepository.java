package com.example.seat_service.repository;
import com.example.seat_service.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    // uses idx_venue_city
    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.city.id = :cityId")
    List<Venue> findAllByCityId(@Param("cityId") Long cityId);

    // uses idx_venue_active
    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.active = :active")
    List<Venue> findAllByIsActive(@Param("active") Boolean active);  // was @Param("isActive")

    // uses idx_venue_city_active
    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.city.id = :cityId AND v.active = :active")
    List<Venue> findAllByCityIdAndIsActive(@Param("cityId") Long cityId, @Param("active") Boolean active);  // was @Param("isActive")

    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.active = true ORDER BY v.totalCapacity DESC")
    List<Venue> findTopVenuesByCapacity();

    @Query("SELECT v FROM Venue v JOIN FETCH v.city")
    List<Venue> findAllWithCity();

    boolean existsByNameAndCity_Id(String name, Long cityId);
}
