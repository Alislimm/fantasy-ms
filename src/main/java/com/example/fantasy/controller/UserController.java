package com.example.fantasy.controller;

import com.example.fantasy.domain.Lineup;
import com.example.fantasy.domain.Transfer;
import com.example.fantasy.domain.User;
import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.dto.FantasyDtos;
import com.example.fantasy.dto.UserDtos;
import com.example.fantasy.service.FantasyTeamService;
import com.example.fantasy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final FantasyTeamService fantasyTeamService;

    public UserController(UserService userService, FantasyTeamService fantasyTeamService) {
        this.userService = userService;
        this.fantasyTeamService = fantasyTeamService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Validated @RequestBody UserDtos.UserRegisterRequest req) {
        return ResponseEntity.ok(userService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@Validated @RequestBody UserDtos.LoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    @PostMapping("/team")
    public ResponseEntity<FantasyTeam> createTeam(@Validated @RequestBody FantasyDtos.FantasyTeamCreateRequest req) {
        return ResponseEntity.ok(fantasyTeamService.createTeam(req));
    }

    @PostMapping("/lineup")
    public ResponseEntity<Lineup> setLineup(@Validated @RequestBody FantasyDtos.LineupSelectionRequest req) {
        return ResponseEntity.ok(fantasyTeamService.setLineup(req));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Transfer> transfer(@Validated @RequestBody FantasyDtos.TransferRequest req) {
        return ResponseEntity.ok(fantasyTeamService.makeTransfer(req));
    }

    @PostMapping("/squad/build")
    public ResponseEntity<FantasyTeam> buildInitialSquad(@Validated @RequestBody FantasyDtos.SquadBuildRequest req) {
        return ResponseEntity.ok(fantasyTeamService.buildSquad(req));
    }

    @GetMapping("/{userId}/has-fantasy-team")
    public ResponseEntity<Boolean> hasFantasyTeam(@PathVariable Long userId) {
        boolean hasTeam = userService.hasFantasyTeam(userId);
        return ResponseEntity.ok(hasTeam);
    }

    @GetMapping("/{userId}/fantasy-team")
    public ResponseEntity<FantasyTeam> getFantasyTeam(@PathVariable Long userId) {
        FantasyTeam team = fantasyTeamService.getFantasyTeamByUserId(userId);
        return ResponseEntity.ok(team);
    }

    // Additional mapping for plural form - handles /api/users/{id}/team
    @GetMapping(value = "s/{userId}/team", produces = "application/json")
    public ResponseEntity<FantasyTeam> getUserTeam(@PathVariable Long userId) {
        FantasyTeam team = fantasyTeamService.getFantasyTeamByUserId(userId);
        return ResponseEntity.ok(team);
    }
}
