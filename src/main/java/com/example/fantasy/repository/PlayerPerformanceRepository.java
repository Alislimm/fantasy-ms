package com.example.fantasy.repository;

import com.example.fantasy.domain.Match;
import com.example.fantasy.domain.PlayerPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerPerformanceRepository extends JpaRepository<PlayerPerformance, Long> {
    List<PlayerPerformance> findByMatchIn(List<Match> matches);
    
    @Query("SELECT pp FROM PlayerPerformance pp " +
           "WHERE pp.player.id = :playerId " +
           "ORDER BY pp.match.gameWeek.number DESC, pp.match.kickoff DESC " +
           "LIMIT :limit")
    List<PlayerPerformance> findRecentPerformancesByPlayer(@Param("playerId") Long playerId, @Param("limit") int limit);
}
