package com.sportrader.SportingCalender.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateResultRequest {
    private Integer homeGoals;
    private Integer awayGoals;
    private String winner;
}