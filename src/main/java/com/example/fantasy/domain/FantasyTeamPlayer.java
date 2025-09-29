package com.example.fantasy.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fantasy_team_player", uniqueConstraints = @UniqueConstraint(columnNames = {"fantasy_team_id", "player_id", "active"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyTeamPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fantasy_team_id", nullable = false)
    @JsonIgnore
    private FantasyTeam fantasyTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private BasketballPlayer player;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private boolean active; // whether currently in squad

    private Instant acquiredAt;
    private Instant releasedAt;
}
