package com.example.fantasy.domain;

import com.example.fantasy.domain.enums.GameWeekStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_week", uniqueConstraints = @UniqueConstraint(columnNames = {"number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer number;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameWeekStatus status;

    @OneToMany(mappedBy = "gameWeek", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Match> fixtures = new ArrayList<>();
}
