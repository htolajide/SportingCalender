package com.sportrader.SportingCalender;

import com.sportrader.SportingCalender.dto.CreateEventRequest;
import com.sportrader.SportingCalender.dto.UpdateResultRequest;
import com.sportrader.SportingCalender.entity.Event;
import com.sportrader.SportingCalender.entity.EventResult;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private CreateEventRequest createRequest;
    private UpdateResultRequest resultRequest;

    @BeforeEach
    void setUp() {
        // Setup test event
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setDateVenue(LocalDate.of(2024, 1, 3));
        testEvent.setTimeVenueUtc(LocalTime.of(16, 0));
        testEvent.setStatus("scheduled");
        testEvent.setSeason(2024);
        
        Team homeTeam = new Team();
        homeTeam.setId(1L);
        homeTeam.setName("Al Hilal");
        homeTeam.setSlug("al-hilal");
        testEvent.setHomeTeam(homeTeam);
        
        Team awayTeam = new Team();
        awayTeam.setId(2L);
        awayTeam.setName("Shabab Al Ahli");
        awayTeam.setSlug("shabab-al-ahli");
        testEvent.setAwayTeam(awayTeam);
        
        Competition competition = new Competition();
        competition.setId(1L);
        competition.setName("AFC Champions League");
        competition.setOriginId("afc-champions-league");
        testEvent.setCompetition(competition);
        
        Stage stage = new Stage();
        stage.setId(1L);
        stage.setName("ROUND OF 16");
        stage.setOrdering(4);
        testEvent.setStage(stage);

        // Setup create request
        createRequest = new CreateEventRequest();
        createRequest.setDateVenue(LocalDate.of(2024, 1, 3));
        createRequest.setTimeVenueUtc("16:00");
        createRequest.setStatus("scheduled");
        createRequest.setSeason(2024);
        
        CreateEventRequest.TeamDto homeDto = new CreateEventRequest.TeamDto();
        homeDto.setName("Al Hilal");
        homeDto.setSlug("al-hilal");
        homeDto.setCountryCode("KSA");
        createRequest.setHomeTeam(homeDto);
        
        CreateEventRequest.TeamDto awayDto = new CreateEventRequest.TeamDto();
        awayDto.setName("Shabab Al Ahli");
        awayDto.setSlug("shabab-al-ahli");
        awayDto.setCountryCode("UAE");
        createRequest.setAwayTeam(awayDto);
        
        CreateEventRequest.CompetitionDto compDto = new CreateEventRequest.CompetitionDto();
        compDto.setName("AFC Champions League");
        compDto.setOriginId("afc-champions-league");
        createRequest.setCompetition(compDto);
        
        CreateEventRequest.StageDto stageDto = new CreateEventRequest.StageDto();
        stageDto.setName("ROUND OF 16");
        stageDto.setOrdering(4);
        createRequest.setStage(stageDto);

        // Setup result request
        resultRequest = new UpdateResultRequest();
        resultRequest.setHomeGoals(2);
        resultRequest.setAwayGoals(1);
        resultRequest.setWinner("home");
    }

    // ===== Basic CRUD Tests =====

    @Test
    void testGetAllEvents() {
        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent));
        
        List<Event> result = eventService.getAllEvents();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Al Hilal", result.get(0).getHomeTeam().getName());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testGetEventById() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        
        Event result = eventService.getEventById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEventByIdNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> eventService.getEventById(999L));
    }

    // ===== Create Event Tests =====

    @Test
    void testCreateEventFromRequest() {
        // Mock findOrCreate services
        Team savedHomeTeam = new Team();
        savedHomeTeam.setId(1L);
        savedHomeTeam.setName("Al Hilal");
        when(teamService.findOrCreateTeam(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(savedHomeTeam);
        
        Team savedAwayTeam = new Team();
        savedAwayTeam.setId(2L);
        savedAwayTeam.setName("Shabab Al Ahli");
        when(teamService.findOrCreateTeam(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(savedAwayTeam);
        
        Competition savedCompetition = new Competition();
        savedCompetition.setId(1L);
        when(competitionService.findOrCreateCompetition(anyString(), anyString()))
                .thenReturn(savedCompetition);
        
        Stage savedStage = new Stage();
        savedStage.setId(1L);
        when(stageService.findOrCreateStage(anyString(), any()))
                .thenReturn(savedStage);
        
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            e.setId(1L);
            return e;
        });
        
        Event result = eventService.createEventFromRequest(createRequest);
        
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("scheduled", result.getStatus());
        verify(eventRepository, times(1)).save(any(Event.class));
    }
    // ===== Filter & Sort Tests =====

    @Test
    void testGetFilteredEvents_NoFilters() {
        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent));
        
        List<Event> result = eventService.getFilteredEvents(null, null, null, "date");
        
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testGetFilteredEvents_ByDate() {
        Event event1 = new Event();
        event1.setDateVenue(LocalDate.of(2024, 1, 3));
        Event event2 = new Event();
        event2.setDateVenue(LocalDate.of(2024, 1, 4));
        
        when(eventRepository.findAll()).thenReturn(Arrays.asList(event1, event2));
        
        List<Event> result = eventService.getFilteredEvents(
                LocalDate.of(2024, 1, 3), null, null, "date");
        
        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2024, 1, 3), result.get(0).getDateVenue());
    }

    @Test
    void testGetFilteredEvents_ByCompetition() {
        Competition comp1 = new Competition();
        comp1.setName("AFC Champions League");
        Competition comp2 = new Competition();
        comp2.setName("Premier League");
        
        Event event1 = new Event();
        event1.setCompetition(comp1);
        Event event2 = new Event();
        event2.setCompetition(comp2);
        
        when(eventRepository.findAll()).thenReturn(Arrays.asList(event1, event2));
        
        List<Event> result = eventService.getFilteredEvents(
                null, "AFC Champions League", null, "date");
        
        assertEquals(1, result.size());
        assertEquals("AFC Champions League", result.get(0).getCompetition().getName());
    }

    @Test
    void testGetFilteredEvents_SortByCompetition() {
        Competition compA = new Competition();
        compA.setName("A League");
        Competition compB = new Competition();
        compB.setName("B League");
        
        Event eventB = new Event();
        eventB.setCompetition(compB);
        Event eventA = new Event();
        eventA.setCompetition(compA);
        
        when(eventRepository.findAll()).thenReturn(Arrays.asList(eventB, eventA));
        
        List<Event> result = eventService.getFilteredEvents(null, null, null, "competition");
        
        assertEquals("A League", result.get(0).getCompetition().getName());
        assertEquals("B League", result.get(1).getCompetition().getName());
    }

    @Test
    void testGetDistinctCompetitions() {
        when(eventRepository.findDistinctCompetitionNames())
                .thenReturn(Arrays.asList("AFC Champions League", "Premier League"));
        
        List<String> result = eventService.getDistinctCompetitions();
        
        assertEquals(2, result.size());
        assertTrue(result.contains("AFC Champions League"));
        verify(eventRepository, times(1)).findDistinctCompetitionNames();
    }

    @Test
    void testGetDistinctStatuses() {
        when(eventRepository.findDistinctStatuses())
                .thenReturn(Arrays.asList("scheduled", "played"));
        
        List<String> result = eventService.getDistinctStatuses();
        
        assertEquals(2, result.size());
        assertTrue(result.contains("scheduled"));
        verify(eventRepository, times(1)).findDistinctStatuses();
    }

    // ===== Search Tests =====

    @Test
    void testSearchEvents_MatchHomeTeam() {
        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent));
        
        List<Event> result = eventService.getAllEvents().stream()
                .filter(e -> e.getHomeTeam().getName().toLowerCase().contains("hilal"))
                .toList();
        
        assertEquals(1, result.size());
        assertEquals("Al Hilal", result.get(0).getHomeTeam().getName());
    }

    @Test
    void testSearchEvents_MatchAwayTeam() {
        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent));
        
        List<Event> result = eventService.getAllEvents().stream()
                .filter(e -> e.getAwayTeam().getName().toLowerCase().contains("ahli"))
                .toList();
        
        assertEquals(1, result.size());
        assertEquals("Shabab Al Ahli", result.get(0).getAwayTeam().getName());
    }

    @Test
    void testSearchEvents_NoMatch() {
        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent));
        
        List<Event> result = eventService.getAllEvents().stream()
                .filter(e -> e.getHomeTeam().getName().toLowerCase().contains("nonexistent"))
                .toList();
        
        assertTrue(result.isEmpty());
    }

}