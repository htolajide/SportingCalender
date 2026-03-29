package com.sportrader.SportingCalender;

import com.sportrader.SportingCalender.controller.EventController;
import com.sportrader.SportingCalender.dto.CreateEventRequest;
import com.sportrader.SportingCalender.dto.UpdateResultRequest;
import com.sportrader.SportingCalender.entity.Event;
import com.sportrader.SportingCalender.entity.Team;
import com.sportrader.SportingCalender.entity.Competition;
import com.sportrader.SportingCalender.entity.Stage;
import com.sportrader.SportingCalender.service.EventService;
import com.sportrader.SportingCalender.service.TeamService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;
    
    @MockBean
    private TeamService teamService;

    private Event testEvent;
    private CreateEventRequest createRequest;
    private UpdateResultRequest resultRequest;

    @BeforeEach
    void setUp() {
        // Setup test event
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setDateVenue(LocalDate.of(2024, 1, 3));
        testEvent.setTimeVenueUtc(java.time.LocalTime.of(16, 0));
        testEvent.setStatus("scheduled");
        testEvent.setSeason(2024);
        
        Team homeTeam = new Team();
        homeTeam.setName("Al Hilal");
        homeTeam.setSlug("al-hilal");
        testEvent.setHomeTeam(homeTeam);
        
        Team awayTeam = new Team();
        awayTeam.setName("Shabab Al Ahli");
        awayTeam.setSlug("shabab-al-ahli");
        testEvent.setAwayTeam(awayTeam);
        
        Competition competition = new Competition();
        competition.setName("AFC Champions League");
        competition.setOriginId("afc-champions-league");
        testEvent.setCompetition(competition);
        
        Stage stage = new Stage();
        stage.setName("ROUND OF 16");
        stage.setOrdering(4);
        testEvent.setStage(stage);

        // Setup create request DTO
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

        // Setup result request DTO
        resultRequest = new UpdateResultRequest();
        resultRequest.setHomeGoals(2);
        resultRequest.setAwayGoals(1);
        resultRequest.setWinner("home");
    }

    // ===== GET Endpoints =====

    @Test
    void testGetAllEvents() throws Exception {
        when(eventService.getAllEvents()).thenReturn(Arrays.asList(testEvent));
        
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].homeTeam.name").value("Al Hilal"));
    }

    @Test
    void testGetEventById() throws Exception {
        when(eventService.getEventById(1L)).thenReturn(testEvent);
        
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("scheduled"));
    }

    @Test
    void testGetEventByIdNotFound() throws Exception {
        when(eventService.getEventById(999L)).thenThrow(new RuntimeException("Not found"));
        
        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFilteredEvents() throws Exception {
        when(eventService.getFilteredEvents(
                eq(LocalDate.of(2024, 1, 3)), 
                eq("AFC Champions League"), 
                eq("scheduled"), 
                eq("date")))
                .thenReturn(Arrays.asList(testEvent));
        
        mockMvc.perform(get("/api/events/filter")
                .param("date", "2024-01-03")
                .param("competition", "AFC Champions League")
                .param("status", "scheduled")
                .param("sortBy", "date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCompetitions() throws Exception {
        when(eventService.getDistinctCompetitions())
                .thenReturn(Arrays.asList("AFC Champions League", "Premier League"));
        
        mockMvc.perform(get("/api/events/competitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("AFC Champions League"));
    }

    @Test
    void testGetStatuses() throws Exception {
        when(eventService.getDistinctStatuses())
                .thenReturn(Arrays.asList("scheduled", "played"));
        
        mockMvc.perform(get("/api/events/statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("scheduled"));
    }

    @Test
    void testSearchEvents() throws Exception {
        when(eventService.getAllEvents()).thenReturn(Arrays.asList(testEvent));
        
        mockMvc.perform(get("/api/events/search").param("team", "Hilal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].homeTeam.name").value("Al Hilal"));
    }
    
    @Test
    void testUpdateEvent() throws Exception {
        when(eventService.updateEvent(eq(1L), any(CreateEventRequest.class)))
                .thenReturn(testEvent);
        
        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dateVenue\":\"2024-01-03\",\"timeVenueUtc\":\"16:00\",\"status\":\"played\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}