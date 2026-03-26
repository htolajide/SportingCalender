package com.sportrader.SportingCalender.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonEventData {
    private Integer season;
    private String status;
    private String timeVenueUTC;
    private String dateVenue;
    private String stadium;
    private JsonTeamData homeTeam;
    private JsonTeamData awayTeam;
    private JsonResultData result;
    private JsonStageData stage;
    private String originCompetitionId;
    private String originCompetitionName;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonTeamData {
        private String name;
        private String officialName;
        private String slug;
        private String abbreviation;
        private String teamCountryCode;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonResultData {
        private Integer homeGoals;
        private Integer awayGoals;
        private String winner;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonStageData {
        private String id;
        private String name;
        private Integer ordering;
    }
}