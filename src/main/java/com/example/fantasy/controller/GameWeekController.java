package com.example.fantasy.controller;

import com.example.fantasy.domain.GameWeek;
import com.example.fantasy.domain.Match;
import com.example.fantasy.domain.enums.GameWeekStatus;
import com.example.fantasy.service.GameWeekService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/gameweek")
public class GameWeekController {

    private final GameWeekService gameWeekService;

    public GameWeekController(GameWeekService gameWeekService) {
        this.gameWeekService = gameWeekService;
    }

    @PostMapping("/{id}/calculate")
    public ResponseEntity<Integer> calculate(@PathVariable("id") Long gameWeekId) {
        return ResponseEntity.ok(gameWeekService.calculateGameWeekPoints(gameWeekId));
    }

    @PostMapping
    public ResponseEntity<GameWeek> createGameWeek(@Validated @RequestBody CreateGameWeekRequest request) {
        GameWeek gameWeek = gameWeekService.createGameWeek(
                request.number(),
                request.startDate(),
                request.endDate(),
                request.status()
        );
        return ResponseEntity.ok(gameWeek);
    }

    @GetMapping
    public ResponseEntity<List<GameWeek>> getAllGameWeeks() {
        return ResponseEntity.ok(gameWeekService.getAllGameWeeks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameWeek> getGameWeek(@PathVariable Long id) {
        return ResponseEntity.ok(gameWeekService.getGameWeek(id));
    }

    @PostMapping("/{id}/fixtures")
    public ResponseEntity<Match> addFixture(@PathVariable Long id, @Validated @RequestBody AddFixtureRequest request) {
        Match match = gameWeekService.addFixture(
                id,
                request.homeTeamId(),
                request.awayTeamId(),
                request.kickoff(),
                request.venue()
        );
        return ResponseEntity.ok(match);
    }

    @GetMapping("/{id}/fixtures")
    public ResponseEntity<List<Match>> getGameWeekFixtures(@PathVariable Long id) {
        return ResponseEntity.ok(gameWeekService.getGameWeekFixtures(id));
    }

    @GetMapping("/{id}/fixtures/formatted")
    public ResponseEntity<List<String>> getGameWeekFixturesFormatted(@PathVariable Long id) {
        return ResponseEntity.ok(gameWeekService.getGameWeekFixturesFormatted(id));
    }

    @PostMapping("/initialize/gameweek-1")
    public ResponseEntity<GameWeek> initializeGameWeekOne() {
        return ResponseEntity.ok(gameWeekService.initializeGameWeekOne());
    }

    public record CreateGameWeekRequest(
            @NotNull Integer number,
            LocalDate startDate,
            LocalDate endDate,
            GameWeekStatus status
    ) {}

    public record AddFixtureRequest(
            @NotNull Long homeTeamId,
            @NotNull Long awayTeamId,
            @NotNull Instant kickoff,
            String venue
    ) {}
}
