package com.example.seat_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventScheduler {

    private final EventService eventService;

    // Runs every minute
    @Scheduled(fixedRateString = "${scheduler.event.activation.rate:60000}")
    public void activateScheduledEvents() {
        log.debug("Running scheduled task to activate draft events...");
        eventService.activateScheduledEvents();
    }

    @Scheduled(fixedRateString = "${scheduler.event.completion.rate:60000}")
    public void completePastEvents() {
        log.debug("Running scheduled task to complete past events...");
        eventService.completePastEvents();
    }
}
