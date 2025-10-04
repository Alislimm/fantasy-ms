package com.example.fantasy.controller;

import com.example.fantasy.domain.BasketballPlayer;
import com.example.fantasy.domain.BasketballTeam;
import com.example.fantasy.dto.AdminDtos;
import com.example.fantasy.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    //todo: add security
    @PostMapping("/teams")
    public ResponseEntity<BasketballTeam> createTeam(@Validated @RequestBody AdminDtos.TeamUpsertRequest req) {
        return ResponseEntity.ok(adminService.upsertTeam(null, req));
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<BasketballTeam> updateTeam(@PathVariable Long id, @Validated @RequestBody AdminDtos.TeamUpsertRequest req) {
        return ResponseEntity.ok(adminService.upsertTeam(id, req));
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        adminService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/players")
    public ResponseEntity<BasketballPlayer> createPlayer(@Validated @RequestBody AdminDtos.PlayerUpsertRequest req) {
        return ResponseEntity.ok(adminService.upsertPlayer(null, req));
    }

    @PutMapping("/players/{id}")
    public ResponseEntity<BasketballPlayer> updatePlayer(@PathVariable Long id, @Validated @RequestBody AdminDtos.PlayerUpsertRequest req) {
        return ResponseEntity.ok(adminService.upsertPlayer(id, req));
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        adminService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
