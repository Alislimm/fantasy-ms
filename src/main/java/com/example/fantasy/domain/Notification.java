package com.example.fantasy.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false)
    @Builder.Default
    private Boolean readStatus = false;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // e.g., "TRANSFER", "GAMEWEEK", "PRICE_CHANGE"

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    public enum NotificationType {
        TRANSFER_SUCCESSFUL,
        TRANSFER_FAILED,
        GAMEWEEK_STARTED,
        GAMEWEEK_ENDED,
        POINTS_CALCULATED,
        PRICE_CHANGE,
        LEAGUE_INVITATION,
        SYSTEM_ANNOUNCEMENT
    }
}