package com.example.fantasy.service;

import com.example.fantasy.domain.GameWeek;
import com.example.fantasy.domain.enums.GameWeekStatus;
import com.example.fantasy.repository.GameWeekRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class SchedulerService {

    private final GameWeekRepository gwRepo;
    private final GameWeekService gameWeekService;

    public SchedulerService(GameWeekRepository gwRepo, GameWeekService gameWeekService) {
        this.gwRepo = gwRepo;
        this.gameWeekService = gameWeekService;
    }

    // Every hour: if there is an ACTIVE GW that ended, mark COMPLETED.
    @Scheduled(cron = "0 0 * * * *")
    public void closeCompletedGameWeeks() {
        gwRepo.findAll().forEach(gw -> {
            if (gw.getStatus() == GameWeekStatus.ACTIVE && gw.getEndDate() != null && gw.getEndDate().isBefore(LocalDate.now())) {
                gw.setStatus(GameWeekStatus.COMPLETED);
                gwRepo.save(gw);
            }
        });
    }

    // Every day at 02:00: recalculate points for completed GWs of the last 3 days (simple heuristic)
    @Scheduled(cron = "0 0 2 * * *")
    public void recalcRecentGameWeeks() {
        gwRepo.findAll().stream()
                .filter(gw -> gw.getStatus() == GameWeekStatus.COMPLETED)
                .forEach(gw -> gameWeekService.calculateGameWeekPoints(gw.getId()));
    }
}