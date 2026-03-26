package com.sportrader.SportingCalender.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "official_name")
    private String officialName;

    @Column(nullable = false, unique = true)
    private String slug;

    private String abbreviation;

    @Column(name = "country_code")
    private String countryCode;

    @OneToMany(mappedBy = "homeTeam")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Event> homeEvents = new ArrayList<>();

    @OneToMany(mappedBy = "awayTeam")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Event> awayEvents = new ArrayList<>();
}
