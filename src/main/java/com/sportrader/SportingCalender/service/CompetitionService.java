package com.sportrader.SportingCalender.service;

import com.sportrader.SportingCalender.entity.Competition;
import com.sportrader.SportingCalender.reppository.CompetitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    public CompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    public Competition findOrCreateCompetition(String name, String originId) {
        return competitionRepository.findByOriginId(originId)
                .orElseGet(() -> {
                    Competition newCompetition = new Competition();
                    newCompetition.setName(name);
                    newCompetition.setOriginId(originId);
                    return competitionRepository.save(newCompetition);
                });
    }
}