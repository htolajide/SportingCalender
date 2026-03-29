package com.sportrader.SportingCalender.controller;

import com.sportrader.SportingCalender.dto.CreateEventRequest;
import com.sportrader.SportingCalender.dto.TeamDTO;
import com.sportrader.SportingCalender.dto.UpdateResultRequest;
import com.sportrader.SportingCalender.entity.Event;
import com.sportrader.SportingCalender.entity.Team;
import com.sportrader.SportingCalender.service.EventService;
import com.sportrader.SportingCalender.service.TeamService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;
    private final TeamService teamService;

    public EventController(EventService eventService, TeamService teamService) {
        this.eventService = eventService;
        this.teamService = teamService;
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

    @PostMapping("/create")
    public ResponseEntity<?> createEventFromForm(@RequestBody CreateEventRequest request) {
        System.out.println("=== Creating Event ===");
        System.out.println("Time received: " + request.getTimeVenueUtc());
        System.out.println("Date received: " + request.getDateVenue());
        try {
            Event savedEvent = eventService.createEventFromRequest(request);
            return ResponseEntity.status(201).body(savedEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // In EventController.java

    @PutMapping("/{id}/result")
    public ResponseEntity<?> updateEventResult(
            @PathVariable Long id, 
            @RequestBody UpdateResultRequest request) {  // ✅ Correct DTO
        
        try {
            Event updatedEvent = eventService.updateEventResult(id, request);
            return ResponseEntity.ok(updatedEvent);
        } catch (RuntimeException e) {
            // Handle "not found" gracefully
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error updating result: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody CreateEventRequest request) {
        try {
            Event updatedEvent = eventService.updateEvent(id, request);
            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Event>> getFilteredEvents(
            @RequestParam(required = false) java.time.LocalDate date,
            @RequestParam(required = false) String competition,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "date") String sortBy) {
        return ResponseEntity.ok(eventService.getFilteredEvents(date, competition, status, sortBy));
    }

    @GetMapping("/competitions")
    public ResponseEntity<List<String>> getCompetitions() {
        return ResponseEntity.ok(eventService.getDistinctCompetitions());
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatuses() {
        return ResponseEntity.ok(eventService.getDistinctStatuses());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String team) {
        List<Event> allEvents = eventService.getAllEvents();
        List<Event> filtered = allEvents.stream()
                .filter(e -> (e.getHomeTeam() != null
                        && e.getHomeTeam().getName().toLowerCase().contains(team.toLowerCase())) ||
                        (e.getAwayTeam() != null
                                && e.getAwayTeam().getName().toLowerCase().contains(team.toLowerCase())))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(filtered);
    }

    // Add this endpoint to get all teams for dropdown
    @GetMapping("/teams")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        List<TeamDTO> teamDTOs = teams.stream()
                .map(t -> new TeamDTO(t.getId(), t.getName(), t.getSlug(), t.getCountryCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(teamDTOs);
    }
}