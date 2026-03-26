package com.sportrader.SportingCalender.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_venue", nullable = false)
    private LocalDate dateVenue;

    @Column(name = "time_venue_utc", nullable = false)
    private LocalTime timeVenueUtc;

    @Column(nullable = false)
    private String status;

    private Integer season;

    @Column(name = "stadium_name")
    private String stadiumName;

    @ManyToOne
    @JoinColumn(name = "_home_team_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "_away_team_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Team awayTeam;

    @ManyToOne
    @JoinColumn(name = "_stage_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Stage stage;

    @ManyToOne
    @JoinColumn(name = "_competition_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Competition competition;

    @ManyToOne
    @JoinColumn(name = "_venue_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Venue venue;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EventResult result;
}