package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "scoring_rule", uniqueConstraints = @UniqueConstraint(columnNames = {"metric"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g., POINT, REBOUND, ASSIST, STEAL, BLOCK, TURNOVER, FG_MADE, FG_MISS, FT_MADE, THREE_MADE, DOUBLE_DOUBLE, TRIPLE_DOUBLE
    @Column(nullable = false, length = 40)
    private String metric;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal pointsPerUnit;
}
