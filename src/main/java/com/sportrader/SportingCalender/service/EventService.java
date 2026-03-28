package com.sportrader.SportingCalender.service;

import com.sportrader.SportingCalender.entity.Competition;
import com.sportrader.SportingCalender.entity.Event;
import com.sportrader.SportingCalender.entity.EventResult;
import com.sportrader.SportingCalender.entity.Stage;
import com.sportrader.SportingCalender.entity.Team;
import com.sportrader.SportingCalender.dto.CreateEventRequest;
import com.sportrader.SportingCalender.dto.JsonEventData;
import com.sportrader.SportingCalender.dto.UpdateResultRequest;
import com.sportrader.SportingCalender.repository.EventRepository;
import com.sportrader.SportingCalender.repository.EventResultRepository;
import com.sportrader.SportingCalender.service.CompetitionService;
import com.sportrader.SportingCalender.service.StageService;
import com.sportrader.SportingCalender.service.TeamService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final TeamService teamService;
    private final CompetitionService competitionService;
    private final StageService stageService;

    public EventService(EventRepository eventRepository,
            TeamService teamService,
            CompetitionService competitionService,
            StageService stageService) {
        this.eventRepository = eventRepository;
        this.teamService = teamService;
        this.competitionService = competitionService;
        this.stageService = stageService;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll(); // Uses @EntityGraph for efficiency
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event createEventFromRequest(CreateEventRequest request) {
        Team homeTeam = teamService.findOrCreateTeam(
                request.getHomeTeam().getName(),
                request.getHomeTeam().getName(), // Official name same as name for simplicity
                request.getHomeTeam().getSlug(),
                null, // Abbreviation
                request.getHomeTeam().getCountryCode());

        Team awayTeam = teamService.findOrCreateTeam(
                request.getAwayTeam().getName(),
                request.getAwayTeam().getName(),
                request.getAwayTeam().getSlug(),
                null,
                request.getAwayTeam().getCountryCode());

        Competition competition = competitionService.findOrCreateCompetition(
                request.getCompetition().getName(),
                request.getCompetition().getOriginId());

        Stage stage = stageService.findOrCreateStage(
                request.getStage().getName(),
                request.getStage().getOrdering());

        Event event = new Event();
        event.setDateVenue(request.getDateVenue());
        event.setTimeVenueUtc(parseTime(request.getTimeVenueUtc())); // Append seconds
        event.setStatus(request.getStatus());
        event.setSeason(request.getSeason());
        event.setStadiumName(request.getStadiumName());
        event.setHomeTeam(homeTeam);
        event.setAwayTeam(awayTeam);
        event.setCompetition(competition);
        event.setStage(stage);

        return eventRepository.save(event);
    }

    public List<Event> getFilteredEvents(LocalDate date, String competition, String status, String sortBy) {
        List<Event> events = eventRepository.findAll();

        // Apply filters
        if (date != null) {
            events = events.stream()
                    .filter(e -> e.getDateVenue().equals(date))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (competition != null && !competition.isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getCompetition().getName().equalsIgnoreCase(competition))
                    .collect(java.util.stream.Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            events = events.stream()
                    .filter(e -> e.getStatus().equalsIgnoreCase(status))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply sorting
        if ("date".equals(sortBy)) {
            events.sort(java.util.Comparator.comparing(Event::getDateVenue));
        } else if ("competition".equals(sortBy)) {
            events.sort(java.util.Comparator.comparing(e -> e.getCompetition().getName()));
        } else if ("status".equals(sortBy)) {
            events.sort(java.util.Comparator.comparing(Event::getStatus));
        }

        return events;
    }

    public List<String> getDistinctCompetitions() {
        return eventRepository.findDistinctCompetitionNames();
    }

    public List<String> getDistinctStatuses() {
        return eventRepository.findDistinctStatuses();
    }

    public Event updateEventResult(Long eventId, UpdateResultRequest request) {
        Event event = getEventById(eventId);

        if (event.getResult() == null) {
            EventResult result = new EventResult();
            result.setEvent(event);
            event.setResult(result);
        }

        event.getResult().setHomeGoals(request.getHomeGoals());
        event.getResult().setAwayGoals(request.getAwayGoals());
        event.getResult().setWinner(request.getWinner());

        return eventRepository.save(event);
    }

    public Event updateEvent(Long eventId, CreateEventRequest request) {
        Event event = getEventById(eventId);

        // Update teams
        Team homeTeam = teamService.findOrCreateTeam(
                request.getHomeTeam().getName(),
                request.getHomeTeam().getName(),
                request.getHomeTeam().getSlug(),
                null,
                request.getHomeTeam().getCountryCode());

        Team awayTeam = teamService.findOrCreateTeam(
                request.getAwayTeam().getName(),
                request.getAwayTeam().getName(),
                request.getAwayTeam().getSlug(),
                null,
                request.getAwayTeam().getCountryCode());

        Competition competition = competitionService.findOrCreateCompetition(
                request.getCompetition().getName(),
                request.getCompetition().getOriginId());

        Stage stage = stageService.findOrCreateStage(
                request.getStage().getName(),
                request.getStage().getOrdering());

        // Update event fields
        event.setDateVenue(request.getDateVenue());
        event.setTimeVenueUtc(java.time.LocalTime.parse(request.getTimeVenueUtc() + ":00"));
        event.setStatus(request.getStatus());
        event.setSeason(request.getSeason());
        event.setStadiumName(request.getStadiumName());
        event.setHomeTeam(homeTeam);
        event.setAwayTeam(awayTeam);
        event.setCompetition(competition);
        event.setStage(stage);

        return eventRepository.save(event);
    }

    private java.time.LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return java.time.LocalTime.MIDNIGHT;
        }
        try {
            // Try parsing as HH:mm:ss first
            return java.time.LocalTime.parse(timeString);
        } catch (java.time.format.DateTimeParseException e) {
            try {
                // Fallback: parse as HH:mm and add seconds
                return java.time.LocalTime.parse(timeString + ":00");
            } catch (java.time.format.DateTimeParseException e2) {
                // Last resort: return midnight
                return java.time.LocalTime.MIDNIGHT;
            }
        }
    }

    // JSON Import Helper Method
    public Event importEventFromJson(JsonEventData jsonEvent) {
        // Find or create related entities
        Team homeTeam = null;
        if (jsonEvent.getHomeTeam() != null) {
            homeTeam = teamService.findOrCreateTeam(
                    jsonEvent.getHomeTeam().getName(),
                    jsonEvent.getHomeTeam().getOfficialName(),
                    jsonEvent.getHomeTeam().getSlug(),
                    jsonEvent.getHomeTeam().getAbbreviation(),
                    jsonEvent.getHomeTeam().getTeamCountryCode());
        }

        Team awayTeam = null;
        if (jsonEvent.getAwayTeam() != null) {
            awayTeam = teamService.findOrCreateTeam(
                    jsonEvent.getAwayTeam().getName(),
                    jsonEvent.getAwayTeam().getOfficialName(),
                    jsonEvent.getAwayTeam().getSlug(),
                    jsonEvent.getAwayTeam().getAbbreviation(),
                    jsonEvent.getAwayTeam().getTeamCountryCode());
        }

        Competition competition = competitionService.findOrCreateCompetition(
                jsonEvent.getOriginCompetitionName(),
                jsonEvent.getOriginCompetitionId());

        Stage stage = stageService.findOrCreateStage(
                jsonEvent.getStage().getName(),
                jsonEvent.getStage().getOrdering());

        // Build Event entity
        // Validate required team references (DB has non-null foreign keys)
        if (homeTeam == null || awayTeam == null) {
            throw new IllegalArgumentException("Invalid event JSON: homeTeam and awayTeam are required");
        }

        Event event = new Event();
        event.setDateVenue(LocalDate.parse(jsonEvent.getDateVenue()));

        String timeVenueStr = jsonEvent.getTimeVenueUTC();
        if (timeVenueStr == null || timeVenueStr.isBlank()) {
            throw new IllegalArgumentException("Invalid event JSON: timeVenueUTC is required");
        }
        System.out.println(
                "Importing event timeVenueUTC='" + timeVenueStr + "' for event date " + jsonEvent.getDateVenue());
        event.setTimeVenueUtc(LocalTime.parse(timeVenueStr));
        event.setStatus(jsonEvent.getStatus());
        event.setSeason(jsonEvent.getSeason());
        event.setStadiumName(jsonEvent.getStadium());
        event.setHomeTeam(homeTeam);
        event.setAwayTeam(awayTeam);
        event.setStage(stage);
        event.setCompetition(competition);

        // Handle optional result
        if (jsonEvent.getResult() != null) {
            EventResult result = new EventResult();
            result.setEvent(event);
            result.setHomeGoals(jsonEvent.getResult().getHomeGoals());
            result.setAwayGoals(jsonEvent.getResult().getAwayGoals());
            result.setWinner(jsonEvent.getResult().getWinner());
            event.setResult(result);
        }

        return eventRepository.save(event);
    }
}