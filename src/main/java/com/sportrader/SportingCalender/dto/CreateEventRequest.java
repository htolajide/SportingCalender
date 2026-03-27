package com.sportrader.SportingCalender.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateEventRequest {
    private LocalDate dateVenue;
    private String timeVenueUtc; // String to handle "HH:mm" from HTML
    private String status;
    private Integer season;
    private String stadiumName;
    
    private TeamDto homeTeam;
    private TeamDto awayTeam;
    private CompetitionDto competition;
    private StageDto stage;

    @Getter
    @Setter
    public static class TeamDto {
        private String name;
        private String slug;
        private String countryCode;
    }

    @Getter
    @Setter
    public static class CompetitionDto {
        private String name;
        private String originId;
    }

    @Getter
    @Setter
    public static class StageDto {
        private String name;
        private Integer ordering;
    }
}