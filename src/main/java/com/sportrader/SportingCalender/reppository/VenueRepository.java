package com.sportrader.SportingCalender.reppository;

import com.sportrader.SportingCalender.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    // Basic CRUD - extend later if needed
}