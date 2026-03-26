package com.sportrader.SportingCalender.repository;

import com.sportrader.SportingCalender.entity.EventResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventResultRepository extends JpaRepository<EventResult, Long> {
    // Basic CRUD - accessed via Event relationship
}