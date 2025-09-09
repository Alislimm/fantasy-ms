package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "fantasy_league_team", uniqueConstraints = @UniqueConstraint(columnNames = {"league_id", "team_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyLeagueTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private FantasyLeague league;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private FantasyTeam team;

    private Integer totalPoints;
    private Integer rank;

    private Instant joinedAt;
}
