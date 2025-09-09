package com.example.fantasy.controller;

import com.example.fantasy.service.GameWeekService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
