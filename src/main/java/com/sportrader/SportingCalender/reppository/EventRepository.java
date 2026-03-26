package com.sportrader.SportingCalender.reppository;

import com.sportradar.calendar.entity.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Efficient query: Fetch all related entities in ONE query (solves N+1 problem)
    @EntityGraph(attributePaths = {"homeTeam", "awayTeam", "stage", "competition", "venue", "result"})
    List<Event> findAll();

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam", "stage", "competition", "venue", "result"})
    List<Event> findByDateVenue(LocalDate date);

    @EntityGraph(attributePaths = {"homeTeam", "awayTeam", "stage", "competition", "venue", "result"})
    List<Event> findByCompetitionName(String competitionName);
}