package com.example.seat_service.repository;

import com.example.seat_service.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface cityRepository extends JpaRepository<City, Long> {
}
