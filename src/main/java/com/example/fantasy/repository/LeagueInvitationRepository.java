package com.example.fantasy.repository;

import com.example.fantasy.domain.LeagueInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueInvitationRepository extends JpaRepository<LeagueInvitation, Long> {
    Optional<LeagueInvitation> findByToken(String token);
}
