package com.sportrader.SportingCalender.entity;

import jakarta.persistence.*;
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
    private Event event;

    private Integer homeGoals;
    private Integer awayGoals;
    private String winner;
}
