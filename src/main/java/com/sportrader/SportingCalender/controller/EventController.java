package com.sportrader.SportingCalender.controller;

import com.sportrader.SportingCalender.dto.CreateEventRequest;
import com.sportrader.SportingCalender.entity.Event;
import com.sportrader.SportingCalender.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*") // Allow frontend access
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        try {
            Event event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event savedEvent = eventService.createEvent(event);
        return ResponseEntity.status(201).body(savedEvent);
    }

    // Add to EventController.java
    @PostMapping("/create")
    public ResponseEntity<Event> createEventFromForm(@RequestBody CreateEventRequest request) {
        try {
            Event savedEvent = eventService.createEventFromRequest(request);
            return ResponseEntity.status(201).body(savedEvent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}