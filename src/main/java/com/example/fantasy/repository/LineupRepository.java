package com.example.fantasy.repository;

import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.domain.GameWeek;
import com.example.fantasy.domain.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LineupRepository extends JpaRepository<Lineup, Long> {
    Optional<Lineup> findByFantasyTeamAndGameWeek(FantasyTeam team, GameWeek gw);
}
