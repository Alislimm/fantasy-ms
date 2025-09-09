package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_performance", uniqueConstraints = @UniqueConstraint(columnNames = {"match_id", "player_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private BasketballPlayer player;

    // Basic stats
    private Integer minutes;
    private Integer points;
    private Integer rebounds;
    private Integer assists;
    private Integer steals;
    private Integer blocks;
    private Integer turnovers;

    private Integer fgMade;
    private Integer fgAttempted;
    private Integer threeMade;
    private Integer threeAttempted;
    private Integer ftMade;
    private Integer ftAttempted;

    // Precomputed fantasy points for the match
    private Integer fantasyPoints;
}
