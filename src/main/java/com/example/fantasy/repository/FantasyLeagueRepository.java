package com.example.fantasy.repository;

import com.example.fantasy.domain.FantasyLeague;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FantasyLeagueRepository extends JpaRepository<FantasyLeague, Long> {
    Optional<FantasyLeague> findByInviteCode(String inviteCode);
    Optional<FantasyLeague> findByName(String name);
}
