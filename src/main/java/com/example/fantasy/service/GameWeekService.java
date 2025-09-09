package com.example.fantasy.service;

import com.example.fantasy.domain.*;
import com.example.fantasy.dto.FantasyDtos;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.repository.*;
import com.example.fantasy.util.ScoringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class GameWeekService {

    private final GameWeekRepository gwRepo;
    private final MatchRepository matchRepo;
    private final PlayerPerformanceRepository perfRepo;
    private final ScoringRuleRepository scoringRepo;
    private final LineupRepository lineupRepo;
    private final LineupSlotRepository slotRepo;
    private final FantasyTeamRepository teamRepo;

    public GameWeekService(GameWeekRepository gwRepo, MatchRepository matchRepo, PlayerPerformanceRepository perfRepo,
                           ScoringRuleRepository scoringRepo, LineupRepository lineupRepo, LineupSlotRepository slotRepo,
                           FantasyTeamRepository teamRepo) {
        this.gwRepo = gwRepo;
        this.matchRepo = matchRepo;
        this.perfRepo = perfRepo;
        this.scoringRepo = scoringRepo;
        this.lineupRepo = lineupRepo;
        this.slotRepo = slotRepo;
        this.teamRepo = teamRepo;
    }

    public int calculateGameWeekPoints(Long gameWeekId) {
        GameWeek gw = gwRepo.findById(gameWeekId).orElseThrow(() -> new NotFoundException("GameWeek not found"));
        List<Match> matches = matchRepo.findByGameWeek(gw);
        Map<String, java.math.BigDecimal> ruleMap = ScoringUtil.toRuleMap(scoringRepo.findAll());

        // Precompute points per player in this GW
        Map<Long, Integer> playerPoints = new HashMap<>();
        perfRepo.findByMatchIn(matches).forEach(p -> {
            int fp = ScoringUtil.calculateFantasyPoints(p, ruleMap);
            playerPoints.put(p.getPlayer().getId(), fp);
        });

        // For each lineup, sum starter points (captain double if slotPosition == CPT); bench gets 0
        int totalUpdated = 0;
        for (Lineup l : lineupRepo.findAll()) {
            if (!l.getGameWeek().getId().equals(gw.getId())) continue;
            List<LineupSlot> slots = slotRepo.findByLineup(l);
            int sum = 0;
            for (LineupSlot s : slots) {
                if (Boolean.TRUE.equals(s.isStarter())) {
                    int p = playerPoints.getOrDefault(s.getPlayer().getId(), 0);
                    if ("CPT".equals(s.getSlotPosition())) {
                        p *= 2;
                    }
                    sum += p;
                }
            }
            FantasyTeam team = l.getFantasyTeam();
            team.setTotalPoints(team.getTotalPoints() + sum);
            teamRepo.save(team);
            totalUpdated += sum;
        }
        return totalUpdated;
    }
}
