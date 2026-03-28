package com.sportrader.SportingCalender.service;

import com.sportrader.SportingCalender.entity.Team;
import com.sportrader.SportingCalender.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team findOrCreateTeam(String name, String officialName, String slug,
            String abbreviation, String countryCode) {
        return teamRepository.findBySlug(slug)
                .orElseGet(() -> {
                    Team newTeam = new Team();
                    newTeam.setName(name);
                    newTeam.setOfficialName(officialName);
                    newTeam.setSlug(slug);
                    newTeam.setAbbreviation(abbreviation);
                    newTeam.setCountryCode(countryCode);
                    return teamRepository.save(newTeam);
                });
    }
}