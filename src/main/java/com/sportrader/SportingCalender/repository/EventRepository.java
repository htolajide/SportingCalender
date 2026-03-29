package com.sportrader.SportingCalender.repository;

import com.sportrader.SportingCalender.entity.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = { "homeTeam", "awayTeam", "stage", "competition", "venue", "result" })
    List<Event> findAll();

    @EntityGraph(attributePaths = { "homeTeam", "awayTeam", "stage", "competition", "venue", "result" })
    List<Event> findByDateVenue(LocalDate date);

    @EntityGraph(attributePaths = { "homeTeam", "awayTeam", "stage", "competition", "venue", "result" })
    List<Event> findByCompetitionName(String competitionName);

    @EntityGraph(attributePaths = { "homeTeam", "awayTeam", "stage", "competition", "venue", "result" })
    List<Event> findByStatus(String status);

    @Query("SELECT DISTINCT e.competition.name FROM Event e")
    List<String> findDistinctCompetitionNames();

    @Query("SELECT DISTINCT e.status FROM Event e")
    List<String> findDistinctStatuses();
}