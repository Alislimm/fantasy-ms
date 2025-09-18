package com.example.fantasy.repository;

import com.example.fantasy.domain.PlayerPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerPriceHistoryRepository extends JpaRepository<PlayerPriceHistory, Long> {
    
    List<PlayerPriceHistory> findByPlayerIdOrderByCreatedAtDesc(Long playerId);
    
    List<PlayerPriceHistory> findByGameWeekIdOrderByCreatedAtDesc(Long gameWeekId);
    
    @Query("SELECT pph FROM PlayerPriceHistory pph WHERE pph.player.id = :playerId ORDER BY pph.createdAt DESC LIMIT 1")
    PlayerPriceHistory findLatestByPlayerId(@Param("playerId") Long playerId);
}