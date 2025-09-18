package com.example.fantasy.controller;

import com.example.fantasy.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/{leagueId}")
    public ResponseEntity<List<LeaderboardService.LeaderboardEntry>> getLeagueLeaderboard(@PathVariable Long leagueId) {
        List<LeaderboardService.LeaderboardEntry> leaderboard = leaderboardService.getLeagueLeaderboard(leagueId);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/global")
    public ResponseEntity<List<LeaderboardService.LeaderboardEntry>> getGlobalLeaderboard() {
        List<LeaderboardService.LeaderboardEntry> leaderboard = leaderboardService.getGlobalLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
}