package com.example.fantasy.domain;

import com.example.fantasy.domain.enums.PlayerPosition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "basketball_player")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketballPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private PlayerPosition position;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private BasketballTeam team;

    @Column(length = 80)
    private String nationality;

    @Column(precision = 12, scale = 2)
    private BigDecimal marketValue;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
