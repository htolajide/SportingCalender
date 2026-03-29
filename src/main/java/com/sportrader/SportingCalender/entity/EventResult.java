package com.sportrader.SportingCalender.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Entity
@Table(name = "event_results")
@Data @NoArgsConstructor @AllArgsConstructor
public class EventResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "_event_id", unique = true, nullable = false)
    @JsonIgnore
    private Event event;

    @Column(name = "home_goals")
    private Integer homeGoals;

    @Column(name = "away_goals")
    private Integer awayGoals;

    private String winner;
}