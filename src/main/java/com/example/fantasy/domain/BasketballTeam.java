package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "basketball_team", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketballTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 10)
    private String shortName;

    @Column(length = 100)
    private String city;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "team")
    @Builder.Default
    private Set<BasketballPlayer> players = new HashSet<>();
}
