package com.example.seat_service.service;

import com.example.seat_service.dto.eventSeatStatus.EventSeatStatusResponse;
import com.example.seat_service.dto.event.EventRequest;
import com.example.seat_service.dto.event.EventCardResponse;
import com.example.seat_service.dto.event.EventAdminResponse;
import com.example.seat_service.dto.event.EventDetailResponse;
import com.example.seat_service.entity.*;
import com.example.seat_service.entity.EventCategory;
import com.example.seat_service.repository.EventRepository;
import com.example.seat_service.repository.EventSeatStatusRepository;
import com.example.seat_service.repository.SeatRepository;
import com.example.seat_service.repository.VenueRepository;
import com.example.seat_service.service.mapper.EventMapper;
import com.example.seat_service.service.mapper.EventSeatStatusMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventSeatStatusRepository eventSeatStatusRepository;
    private final EventMapper eventMapper;
    private final EventSeatStatusMapper eventSeatStatusMapper;

    // fix N+1 — batch count for list endpoints
    private List<EventCardResponse> toCardResponseList(List<Event> events) {
        if (events.isEmpty())
            return List.of();
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> countMap = eventSeatStatusRepository
                .countAvailableSeatsForEvents(eventIds, SeatBookingStatus.AVAILABLE)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
        return events.stream()
                .map(event -> eventMapper.toCardResponse(event, countMap.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    private List<EventAdminResponse> toAdminResponseList(List<Event> events) {
        if (events.isEmpty())
            return List.of();
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> countMap = eventSeatStatusRepository
                .countAvailableSeatsForEvents(eventIds, SeatBookingStatus.AVAILABLE)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]));
        return events.stream()
                .map(event -> eventMapper.toAdminResponse(event, countMap.getOrDefault(event.getId(), 0L),
                        (long) event.getVenue().getTotalCapacity()))
                .toList();
    }

    @Cacheable(value = "events", key = "'venue:' + #venueId")
    public List<EventCardResponse> findAllByVenueId(Long venueId) {
        log.info("Cache MISS - fetching events by venueId:{} from DB", venueId);
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with id: " + venueId);
        }
        return toCardResponseList(eventRepository.findAllByVenueId(venueId));
    }

    @Cacheable(value = "events", key = "'range:' + #from + ':' + #to")
    public List<EventCardResponse> findAllByStartTimeBetween(Instant from, Instant to) {
        log.info("Cache MISS - fetching events by range from:{} to:{} from DB", from, to);
        return toCardResponseList(eventRepository.findAllByStartTimeBetween(from, to));
    }

    @Cacheable(value = "events", key = "'status:' + #status + (#category != null ? ':category:' + #category : '')")
    public List<EventCardResponse> findAllByStatus(EventStatus status, EventCategory category) {
        log.info("Cache MISS - fetching events by status:{} category:{} from DB", status, category);
        List<Event> events = eventRepository.findByStatus(status);
        if (category != null) {
            events = events.stream().filter(e -> e.getCategory() == category).toList();
        }
        return toCardResponseList(events);
    }

    @Cacheable(value = "events", key = "'venue:' + #venueId + ':range:' + #from + ':' + #to")
    public List<EventCardResponse> findAllByVenueIdAndStartTimeBetween(Long venueId, Instant from, Instant to) {
        log.info("Cache MISS - fetching events by venueId:{} range from:{} to:{} from DB", venueId, from, to);
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with id: " + venueId);
        }
        return toCardResponseList(eventRepository.findAllByVenueIdAndStartTimeBetween(venueId, from, to));
    }

    @Cacheable(value = "events", key = "'bookable'")
    public List<EventCardResponse> findAllCurrentlyBookable() {
        log.info("Cache MISS - fetching bookable events from DB");
        return toCardResponseList(eventRepository.findAllCurrentlyBookable(Instant.now()));
    }

    @Cacheable(value = "events", key = "'search:' + #query + ':' + #city")
    public List<EventCardResponse> searchEvents(String query, String city) {
        log.info("Cache MISS - searching events query:{} city:{}", query, city);
        return toCardResponseList(eventRepository.searchEvents(query, city, EventStatus.ACTIVE));
    }

    @Cacheable(value = "eventDetail", key = "#id")
    public EventDetailResponse findEventById(Long id) {
        log.info("Cache MISS - fetching event detail id:{} from DB", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
        Long availableSeats = eventSeatStatusRepository.countByEventIdAndStatus(id, SeatBookingStatus.AVAILABLE);
        return eventMapper.toDetailResponse(event, availableSeats);
    }

    @Cacheable(value = "eventsAdmin", key = "'admin:status:' + (#status != null ? #status : 'ALL')")
    public List<EventAdminResponse> findAllByStatusAdmin(EventStatus status) {
        log.info("Cache MISS - fetching admin events by status:{} from DB", status);
        List<Event> events = (status == null)
                ? eventRepository.findAll()
                : eventRepository.findByStatus(status);
            return toAdminResponseList(events);
    }

    @Cacheable(value = "eventsAdmin", key = "'admin:venue:' + #venueId + ':range:' + #from + ':' + #to")
    public List<EventAdminResponse> findAllByVenueIdAndStartTimeBetweenAdmin(Long venueId, Instant from, Instant to) {
        log.info("Cache MISS - fetching admin events by venueId:{} range from:{} to:{} from DB", venueId, from, to);
        if (!venueRepository.existsById(venueId)) {
            throw new EntityNotFoundException("Venue not found with id: " + venueId);
        }
        return toAdminResponseList(eventRepository.findAllByVenueIdAndStartTimeBetween(venueId, from, to));
    }

    @Cacheable(value = "seatMap", key = "#eventId")
    public List<EventSeatStatusResponse> findAllSeatsByEventId(Long eventId) {
        log.info("Cache MISS - fetching seat map for eventId:{} from DB", eventId);
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        return eventSeatStatusRepository.findAllByEventId(eventId)
                .stream()
                .map(eventSeatStatusMapper::toResponse)
                .toList();
    }

    public List<EventSeatStatusResponse> findSeatsByEventIdAndStatus(Long eventId, SeatBookingStatus status) {
        // not cached — available seats change too frequently
        if (!eventRepository.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }
        return eventSeatStatusRepository.findAllByEventIdAndStatus(eventId, status)
                .stream()
                .map(eventSeatStatusMapper::toResponse)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventsAdmin", allEntries = true)
    })
    public EventDetailResponse createEvent(EventRequest request) {
        Venue venue = venueRepository.findById(request.getVenue_id())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + request.getVenue_id()));

        validateBookingWindow(request);

        Event event = eventMapper.toEntity(request, venue);
        Event saved = eventRepository.save(event);

        initializeEventSeats(saved, venue.getId());

        return eventMapper.toDetailResponse(saved, 0L);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventsAdmin", allEntries = true),
            @CacheEvict(value = "eventDetail", key = "#id")
    })
    public EventDetailResponse updateEvent(Long id, EventRequest request) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

        if (!existing.getVenue().getId().equals(request.getVenue_id())) {
            Venue newVenue = venueRepository.findById(request.getVenue_id())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Venue not found with id: " + request.getVenue_id()));
            existing.setVenue(newVenue);
        }

        validateBookingWindow(request);
        eventMapper.updateEntity(existing, request);
        existing.setUpdatedAt(Instant.now());

        Event saved = eventRepository.save(existing);
        Long availableSeats = eventSeatStatusRepository.countByEventIdAndStatus(saved.getId(),
                SeatBookingStatus.AVAILABLE);
        return eventMapper.toDetailResponse(saved, availableSeats);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventsAdmin", allEntries = true),
            @CacheEvict(value = "eventDetail", key = "#id"),
            @CacheEvict(value = "seatMap", key = "#id")
    })
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("Event not found with id: " + id);
        }
        eventSeatStatusRepository.deleteAllByEventId(id);
        eventRepository.deleteById(id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventsAdmin", allEntries = true),
            @CacheEvict(value = "eventDetail", allEntries = true)
    })
    public void activateScheduledEvents() {
        List<Event> draftEvents = eventRepository.findByStatusAndBookingOpenAtBefore(EventStatus.DRAFT, Instant.now());
        if (!draftEvents.isEmpty()) {
            for (Event event : draftEvents) {
                event.setStatus(EventStatus.ACTIVE);
                event.setUpdatedAt(Instant.now());
            }
            eventRepository.saveAll(draftEvents);
            log.info("Activated {} scheduled draft events.", draftEvents.size());
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "eventsAdmin", allEntries = true),
            @CacheEvict(value = "eventDetail", allEntries = true)
    })
    public void completePastEvents() {
        List<Event> eventsToComplete = eventRepository.findByStatusInAndEndTimeBefore(
                List.of(EventStatus.ACTIVE, EventStatus.SOLD_OUT), Instant.now());
        if (!eventsToComplete.isEmpty()) {
            for (Event event : eventsToComplete) {
                event.setStatus(EventStatus.COMPLETED);
                event.setUpdatedAt(Instant.now());
            }
            eventRepository.saveAll(eventsToComplete);
            log.info("Completed {} past events.", eventsToComplete.size());
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void initializeEventSeats(Event event, Long venueId) {
        List<Seat> seats = seatRepository.findAllWithVenue(venueId);

        if (seats.isEmpty()) {
            throw new IllegalStateException("Venue has no seats, cannot create event");
        }

        List<EventSeatStatus> eventSeats = seats.stream()
                .map(seat -> EventSeatStatus.builder()
                        .event(event)
                        .seat(seat)
                        .status(SeatBookingStatus.AVAILABLE)
                        .price(seat.getBasePrice())
                        .version(0)
                        .build())
                .toList();

        eventSeatStatusRepository.saveAll(eventSeats);
    }

    private void validateBookingWindow(EventRequest request) {
        if (request.getBookingOpenAt().isAfter(request.getStartTime())) {
            throw new IllegalStateException("Booking open time cannot be after event start time");
        }
        if (request.getBookingCloseAt().isAfter(request.getEndTime())) {
            throw new IllegalStateException("Booking close time cannot be after event end time");
        }
    }
}
