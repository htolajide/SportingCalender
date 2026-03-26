package com.sportrader.SportingCalender.service;

import com.sportrader.SportingCalender.entity.Stage;
import com.sportrader.SportingCalender.reppository.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StageService {

    private final StageRepository stageRepository;

    public StageService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    public Stage findOrCreateStage(String name, Integer ordering) {
        return stageRepository.findByName(name)
                .orElseGet(() -> {
                    Stage newStage = new Stage();
                    newStage.setName(name);
                    newStage.setOrdering(ordering);
                    return stageRepository.save(newStage);
                });
    }
}