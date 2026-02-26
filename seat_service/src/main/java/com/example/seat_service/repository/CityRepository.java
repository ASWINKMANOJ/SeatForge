package com.example.seat_service.repository;

import com.example.seat_service.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByStateOrderByNameAsc(String state);

    boolean existsByNameAndState(String name, String state);
}
