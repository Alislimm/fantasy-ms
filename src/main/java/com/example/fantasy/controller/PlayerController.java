package com.example.fantasy.controller;

import com.example.fantasy.domain.BasketballPlayer;
import com.example.fantasy.domain.enums.PlayerPosition;
import com.example.fantasy.repository.BasketballPlayerRepository;
import com.example.fantasy.repository.FantasyTeamPlayerRepository;
import com.example.fantasy.repository.FantasyTeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final BasketballPlayerRepository playerRepo;
    private final FantasyTeamPlayerRepository teamPlayerRepo;
    private final FantasyTeamRepository teamRepo;

    public PlayerController(BasketballPlayerRepository playerRepo,
                            FantasyTeamPlayerRepository teamPlayerRepo,
                            FantasyTeamRepository teamRepo) {
        this.playerRepo = playerRepo;
        this.teamPlayerRepo = teamPlayerRepo;
        this.teamRepo = teamRepo;
    }

    @GetMapping
    public ResponseEntity<List<PlayerListItem>> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double ownershipGte,
            @RequestParam(required = false) Double ownershipLte,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        List<BasketballPlayer> players = playerRepo.findByActiveTrue();

        // filter basic
        List<BasketballPlayer> filtered = players.stream()
                .filter(p -> teamId == null || (p.getTeam() != null && p.getTeam().getId().equals(teamId)))
                .filter(p -> position == null || p.getPosition() == PlayerPosition.valueOf(position))
                .filter(p -> minPrice == null || (p.getMarketValue() != null && p.getMarketValue().compareTo(minPrice) >= 0))
                .filter(p -> maxPrice == null || (p.getMarketValue() != null && p.getMarketValue().compareTo(maxPrice) <= 0))
                .collect(Collectors.toList());

        long totalTeams = Math.max(1, teamRepo.count());

        List<PlayerListItem> result = filtered.stream().map(p -> {
            long count = teamPlayerRepo.countActiveTeamsByPlayerId(p.getId());
            double ownership = (count * 100.0) / totalTeams;
            return new PlayerListItem(
                    p.getId(),
                    p.getFirstName(),
                    p.getLastName(),
                    p.getPosition().name(),
                    p.getTeam() != null ? p.getTeam().getId() : null,
                    p.getTeam() != null ? p.getTeam().getName() : null,
                    p.getMarketValue(),
                    Math.round(ownership * 100.0) / 100.0
            );
        }).filter(i -> ownershipGte == null || i.ownershipPct >= ownershipGte)
          .filter(i -> ownershipLte == null || i.ownershipPct <= ownershipLte)
          .collect(Collectors.toList());

        int from = Math.max(0, Math.min(page * size, result.size()));
        int to = Math.max(from, Math.min(from + size, result.size()));
        List<PlayerListItem> pageItems = result.subList(from, to);
        return ResponseEntity.ok(pageItems);
    }

    public static class PlayerListItem {
        public Long id;
        public String firstName;
        public String lastName;
        public String position;
        public Long teamId;
        public String teamName;
        public BigDecimal price;
        public double ownershipPct;

        public PlayerListItem(Long id, String firstName, String lastName, String position, Long teamId, String teamName, BigDecimal price, double ownershipPct) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.position = position;
            this.teamId = teamId;
            this.teamName = teamName;
            this.price = price;
            this.ownershipPct = ownershipPct;
        }
    }
}
