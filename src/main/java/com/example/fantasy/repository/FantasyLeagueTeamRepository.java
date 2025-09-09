package com.example.fantasy.repository;

import com.example.fantasy.domain.FantasyLeague;
import com.example.fantasy.domain.FantasyLeagueTeam;
import com.example.fantasy.domain.FantasyTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FantasyLeagueTeamRepository extends JpaRepository<FantasyLeagueTeam, Long> {
    Optional<FantasyLeagueTeam> findByLeagueAndTeam(FantasyLeague league, FantasyTeam team);
}
