package com.sportrader.SportingCalender.service;

import com.sportrader.SportingCalender.entity.Competition;
import com.sportrader.SportingCalender.entity.Event;
import com.sportrader.SportingCalender.entity.EventResult;
import com.sportrader.SportingCalender.entity.Stage;
import com.sportrader.SportingCalender.entity.Team;
import com.sportrader.SportingCalender.dto.JsonEventData;
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
                    jsonEvent.getHomeTeam().getTeamCountryCode()
            );
        }

        Team awayTeam = null;
        if (jsonEvent.getAwayTeam() != null) {
            awayTeam = teamService.findOrCreateTeam(
                    jsonEvent.getAwayTeam().getName(),
                    jsonEvent.getAwayTeam().getOfficialName(),
                    jsonEvent.getAwayTeam().getSlug(),
                    jsonEvent.getAwayTeam().getAbbreviation(),
                    jsonEvent.getAwayTeam().getTeamCountryCode()
            );
        }

        Competition competition = competitionService.findOrCreateCompetition(
                jsonEvent.getOriginCompetitionName(),
                jsonEvent.getOriginCompetitionId()
        );

        Stage stage = stageService.findOrCreateStage(
                jsonEvent.getStage().getName(),
                jsonEvent.getStage().getOrdering()
        );

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
        System.out.println("Importing event timeVenueUTC='" + timeVenueStr + "' for event date " + jsonEvent.getDateVenue());
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