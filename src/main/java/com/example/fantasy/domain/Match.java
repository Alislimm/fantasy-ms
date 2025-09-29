package com.example.fantasy.domain;

import com.example.fantasy.domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_week_id", nullable = false)
    private GameWeek gameWeek;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private BasketballTeam homeTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private BasketballTeam awayTeam;

    @Column(nullable = false)
    private Instant kickoff;

    private String score;

    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status;
}
