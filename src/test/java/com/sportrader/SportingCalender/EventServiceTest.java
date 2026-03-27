package com.sportrader.SportingCalender;

import com.sportrader.SportingCalender.entity.Event;
import com.sportrader.SportingCalender.entity.Team;
import com.sportrader.SportingCalender.entity.Competition;
import com.sportrader.SportingCalender.entity.Stage;
import com.sportrader.SportingCalender.repository.EventRepository;
import com.sportrader.SportingCalender.service.EventService;
import com.sportrader.SportingCalender.service.TeamService;
import com.sportrader.SportingCalender.service.CompetitionService;
import com.sportrader.SportingCalender.service.StageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private StageService stageService;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setDateVenue(LocalDate.now());
        testEvent.setTimeVenueUtc(LocalTime.of(18, 0));
        testEvent.setStatus("scheduled");

        Team homeTeam = new Team();
        homeTeam.setName("Team A");
        testEvent.setHomeTeam(homeTeam);

        Team awayTeam = new Team();
        awayTeam.setName("Team B");
        testEvent.setAwayTeam(awayTeam);

        Competition competition = new Competition();
        competition.setName("Test League");
        testEvent.setCompetition(competition);

        Stage stage = new Stage();
        stage.setName("GROUP STAGE");
        testEvent.setStage(stage);
    }

    @Test
    void testGetAllEvents() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testGetEventById() {
        when(eventRepository.findById(1L)).thenReturn(java.util.Optional.of(testEvent));

        Event result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEventByIdNotFound() {
        when(eventRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> eventService.getEventById(999L));
    }
}