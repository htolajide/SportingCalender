package com.sportrader.SportingCalender.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Simple DTO for dropdown (lightweight, no relationships)
@Getter
@AllArgsConstructor
public  class TeamDTO {
    private Long id;
    private String name;
    private String slug;
    private String countryCode;
}