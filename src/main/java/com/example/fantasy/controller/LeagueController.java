package com.example.fantasy.controller;

import com.example.fantasy.domain.FantasyLeague;
import com.example.fantasy.service.LeagueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<FantasyLeague> createLeague(@PathVariable Long userId, @RequestBody CreateLeagueRequest request) {
        FantasyLeague league = leagueService.createLeague(userId, request);
        return ResponseEntity.ok(league);
    }

    @PostMapping("/join-by-code")
    public ResponseEntity<String> joinLeagueByCode(@RequestParam String joinCode, @RequestParam Long userId) {
        leagueService.joinLeagueByCode(joinCode, userId);
        return ResponseEntity.ok("Successfully joined the league!");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserLeagues(@PathVariable Long userId) {
        return ResponseEntity.ok(leagueService.getUserLeagues(userId));
    }

    @GetMapping("/{leagueId}")
    public ResponseEntity<?> getLeagueDetails(@PathVariable Long leagueId) {
        return ResponseEntity.ok(leagueService.getLeagueDetails(leagueId));
    }

    public static class CreateLeagueRequest {
        public String name;

        public CreateLeagueRequest() {}

        public CreateLeagueRequest(String name) {
            this.name = name;
        }
    }
}