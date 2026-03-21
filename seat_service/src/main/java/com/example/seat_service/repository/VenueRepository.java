package com.example.seat_service.repository;

import com.example.seat_service.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue,Long> {
    // uses idx_venue_city
    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.city.id = :cityId")
    List<Venue> findAllByCityId(@Param("cityId") Long cityId);

    // uses idx_venue_active
    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.isActive = :isActive")
    List<Venue> findAllByIsActive(@Param("isActive") Boolean isActive);

    // uses idx_venue_city_active (composite index — both fields must be used together)
    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.city.id = :cityId AND v.isActive = :isActive")
    List<Venue> findAllByCityIdAndIsActive(@Param("cityId") Long cityId, @Param("isActive") Boolean isActive);

    // VenueRepository.java — add this
    @Query("SELECT v FROM Venue v JOIN FETCH v.city WHERE v.isActive = true ORDER BY v.totalCapacity DESC")
    List<Venue> findTopVenuesByCapacity();

    // general findAll with fetch (no index filter)
    @Query("SELECT v FROM Venue v JOIN FETCH v.city")
    List<Venue> findAllWithCity();

    boolean existsByNameAndCity_Id(String name, Long cityId);
}
