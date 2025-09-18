package com.example.fantasy.controller;

import com.example.fantasy.domain.BasketballPlayer;
import com.example.fantasy.domain.BasketballTeam;
import com.example.fantasy.repository.BasketballPlayerRepository;
import com.example.fantasy.repository.BasketballTeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final BasketballTeamRepository teamRepo;
    private final BasketballPlayerRepository playerRepo;

    public TeamController(BasketballTeamRepository teamRepo, BasketballPlayerRepository playerRepo) {
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
    }

    @GetMapping
    public ResponseEntity<List<TeamListItem>> list() {
        List<BasketballTeam> teams = teamRepo.findAll();
        
        List<TeamListItem> result = teams.stream().map(team -> 
            new TeamListItem(
                team.getId(),
                team.getName(),
                team.getShortName(),
                team.getCity()
            )
        ).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamListItem> getById(@PathVariable Long id) {
        return teamRepo.findById(id)
                .map(team -> new TeamListItem(
                    team.getId(),
                    team.getName(),
                    team.getShortName(),
                    team.getCity()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<List<TeamPlayerItem>> getTeamPlayers(@PathVariable Long id) {
        // Check if team exists
        if (!teamRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Get all active players for this team
        List<BasketballPlayer> players = playerRepo.findByTeamIdAndActiveTrue(id);
        
        List<TeamPlayerItem> result = players.stream().map(player -> 
            new TeamPlayerItem(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getPosition().name(),
                player.getMarketValue(),
                player.getNationality()
            )
        ).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    public static class TeamListItem {
        public Long id;
        public String name;
        public String shortName;
        public String city;

        public TeamListItem(Long id, String name, String shortName, String city) {
            this.id = id;
            this.name = name;
            this.shortName = shortName;
            this.city = city;
        }
    }

    public static class TeamPlayerItem {
        public Long id;
        public String firstName;
        public String lastName;
        public String position;
        public BigDecimal price;
        public String nationality;

        public TeamPlayerItem(Long id, String firstName, String lastName, String position, BigDecimal price, String nationality) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.position = position;
            this.price = price;
            this.nationality = nationality;
        }
    }
}