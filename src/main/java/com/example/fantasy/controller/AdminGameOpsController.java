package com.example.fantasy.controller;

import com.example.fantasy.domain.*;
import com.example.fantasy.domain.enums.GameWeekStatus;
import com.example.fantasy.domain.enums.MatchStatus;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/ops")
public class AdminGameOpsController {

    private final GameWeekRepository gwRepo;
    private final MatchRepository matchRepo;
    private final PlayerPerformanceRepository perfRepo;
    private final ScoringRuleRepository scoringRepo;
    private final BasketballTeamRepository teamRepo;
    private final BasketballPlayerRepository playerRepo;

    public AdminGameOpsController(GameWeekRepository gwRepo, MatchRepository matchRepo, PlayerPerformanceRepository perfRepo,
                                  ScoringRuleRepository scoringRepo, BasketballTeamRepository teamRepo, BasketballPlayerRepository playerRepo) {
        this.gwRepo = gwRepo;
        this.matchRepo = matchRepo;
        this.perfRepo = perfRepo;
        this.scoringRepo = scoringRepo;
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
    }

    // GameWeek CRUD
    @PostMapping("/gameweeks")
    public ResponseEntity<GameWeek> createGw(@RequestParam int number, @RequestParam(required=false) String startDate,
                                             @RequestParam(required=false) String endDate, @RequestParam(defaultValue = "UPCOMING") String status) {
        GameWeek gw = new GameWeek();
        gw.setNumber(number);
        gw.setStartDate(startDate == null ? null : LocalDate.parse(startDate));
        gw.setEndDate(endDate == null ? null : LocalDate.parse(endDate));
        gw.setStatus(GameWeekStatus.valueOf(status));
        return ResponseEntity.ok(gwRepo.save(gw));
    }

    @PutMapping("/gameweeks/{id}/status")
    public ResponseEntity<GameWeek> updateGwStatus(@PathVariable Long id, @RequestParam String status) {
        GameWeek gw = gwRepo.findById(id).orElseThrow(() -> new NotFoundException("GW not found"));
        gw.setStatus(GameWeekStatus.valueOf(status));
        return ResponseEntity.ok(gwRepo.save(gw));
    }

    @GetMapping("/gameweeks")
    public ResponseEntity<List<GameWeek>> listGw() { return ResponseEntity.ok(gwRepo.findAll()); }

    // Match CRUD (minimal)
    @PostMapping("/matches")
    public ResponseEntity<Match> createMatch(@RequestParam Long gameWeekId, @RequestParam Long homeTeamId,
                                             @RequestParam Long awayTeamId, @RequestParam String kickoff,
                                             @RequestParam(defaultValue = "SCHEDULED") String status,
                                             @RequestParam(required=false) String venue) {
        GameWeek gw = gwRepo.findById(gameWeekId).orElseThrow(() -> new NotFoundException("GW not found"));
        BasketballTeam home = teamRepo.findById(homeTeamId).orElseThrow(() -> new NotFoundException("Home team not found"));
        BasketballTeam away = teamRepo.findById(awayTeamId).orElseThrow(() -> new NotFoundException("Away team not found"));
        Match m = new Match();
        m.setGameWeek(gw);
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        m.setKickoff(Instant.parse(kickoff));
        m.setStatus(MatchStatus.valueOf(status));
        m.setVenue(venue);
        return ResponseEntity.ok(matchRepo.save(m));
    }

    @PutMapping("/matches/{id}")
    public ResponseEntity<Match> updateMatchScore(@PathVariable Long id, @RequestParam String score,
                                                  @RequestParam(required=false) String status) {
        Match m = matchRepo.findById(id).orElseThrow(() -> new NotFoundException("Match not found"));
        m.setScore(score);
        if (status != null) m.setStatus(MatchStatus.valueOf(status));
        return ResponseEntity.ok(matchRepo.save(m));
    }

    // PlayerPerformance CRUD (minimal upsert)
    @PostMapping("/performances")
    public ResponseEntity<PlayerPerformance> upsertPerf(@RequestParam Long matchId, @RequestParam Long playerId,
                                                        @RequestParam Integer points, @RequestParam Integer rebounds,
                                                        @RequestParam Integer assists, @RequestParam(required=false) Integer steals,
                                                        @RequestParam(required=false) Integer blocks, @RequestParam(required=false) Integer turnovers,
                                                        @RequestParam(required=false) Integer threeMade) {
        Match match = matchRepo.findById(matchId).orElseThrow(() -> new NotFoundException("Match not found"));
        BasketballPlayer player = playerRepo.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        PlayerPerformance p = new PlayerPerformance();
        p.setMatch(match);
        p.setPlayer(player);
        p.setPoints(points);
        p.setRebounds(rebounds);
        p.setAssists(assists);
        p.setSteals(steals);
        p.setBlocks(blocks);
        p.setTurnovers(turnovers);
        p.setThreeMade(threeMade);
        return ResponseEntity.ok(perfRepo.save(p));
    }

    // ScoringRule CRUD
    @PostMapping("/scoring")
    public ResponseEntity<ScoringRule> createRule(@RequestParam String metric, @RequestParam java.math.BigDecimal points) {
        ScoringRule rule = new ScoringRule();
        rule.setMetric(metric);
        rule.setPointsPerUnit(points);
        return ResponseEntity.ok(scoringRepo.save(rule));
    }

    @GetMapping("/scoring")
    public ResponseEntity<List<ScoringRule>> listRules() {
        return ResponseEntity.ok(scoringRepo.findAll());
    }
}