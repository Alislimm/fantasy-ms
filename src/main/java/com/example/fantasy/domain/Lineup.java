package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lineup", uniqueConstraints = @UniqueConstraint(columnNames = {"fantasy_team_id", "game_week_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lineup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fantasy_team_id", nullable = false)
    private FantasyTeam fantasyTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_week_id", nullable = false)
    private GameWeek gameWeek;

    private Instant createdAt;

    @OneToMany(mappedBy = "lineup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<LineupSlot> slots = new HashSet<>();
}
