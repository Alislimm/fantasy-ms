package com.example.fantasy.domain;

import com.example.fantasy.domain.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "league_invitation", indexes = {
        @Index(name = "idx_invite_token", columnList = "token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private FantasyLeague league;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invited_by_user_id", nullable = false)
    private User invitedBy;

    @Column(nullable = false, length = 120)
    private String email; // invited email

    @Column(nullable = false, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status;

    private Instant createdAt;
    private Instant respondedAt;
}
