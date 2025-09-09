package com.example.fantasy.domain;

import com.example.fantasy.domain.enums.LeagueType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fantasy_league")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantasyLeague {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeagueType type;

    @Column(length = 64, unique = true)
    private String inviteCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<FantasyLeagueTeam> teams = new HashSet<>();
}
