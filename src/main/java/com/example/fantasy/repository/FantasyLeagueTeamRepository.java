package com.example.fantasy.repository;

import com.example.fantasy.domain.FantasyLeague;
import com.example.fantasy.domain.FantasyLeagueTeam;
import com.example.fantasy.domain.FantasyTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FantasyLeagueTeamRepository extends JpaRepository<FantasyLeagueTeam, Long> {
    Optional<FantasyLeagueTeam> findByLeagueAndTeam(FantasyLeague league, FantasyTeam team);
    
    @Query("SELECT flt.team.id FROM FantasyLeagueTeam flt WHERE flt.league.id = :leagueId")
    List<Long> findTeamIdsByLeagueId(@Param("leagueId") Long leagueId);
}
