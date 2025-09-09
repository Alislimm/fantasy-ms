package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lineup_slot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineupSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lineup_id", nullable = false)
    private Lineup lineup;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private BasketballPlayer player;

    @Column(nullable = false)
    private boolean starter; // true for starters, false for bench

    @Column(length = 5)
    private String slotPosition; // optional textual slot, e.g., PG, SG, SF, PF, C
}
