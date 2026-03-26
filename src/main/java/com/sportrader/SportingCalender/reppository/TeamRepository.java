package com.sportrader.SportingCalender.reppository;

import com.sportradar.calendar.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findBySlug(@Param("slug") String slug);

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.homeEvents LEFT JOIN FETCH t.awayEvents WHERE t.slug = :slug")
    Optional<Team> findBySlugWithEvents(@Param("slug") String slug);
}