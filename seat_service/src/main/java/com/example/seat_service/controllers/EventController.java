package com.example.seat_service.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/event")
public class EventController {
    @GetMapping
    public ResponseEntity<Map<String, Object>> getEvents() {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    
}
