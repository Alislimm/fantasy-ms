package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "player_price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerPriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private BasketballPlayer player;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_week_id", nullable = false)
    private GameWeek gameWeek;

    @Column(name = "old_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal newPrice;

    @Column(name = "price_change", precision = 12, scale = 2, nullable = false)
    private BigDecimal priceChange;

    @Column(name = "ownership_percentage", precision = 5, scale = 2)
    private BigDecimal ownershipPercentage;

    @Column(name = "performance_score", precision = 8, scale = 2)
    private BigDecimal performanceScore;

    @Column(name = "reason", length = 500)
    private String reason; // Explanation for price change

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}