package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fantasy_team_id", nullable = false)
    private FantasyTeam fantasyTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_week_id", nullable = false)
    private GameWeek gameWeek;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_out_id", nullable = false)
    private BasketballPlayer playerOut;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_in_id", nullable = false)
    private BasketballPlayer playerIn;

    @Column(precision = 12, scale = 2)
    private BigDecimal priceDifference;

    private Instant createdAt;
}
