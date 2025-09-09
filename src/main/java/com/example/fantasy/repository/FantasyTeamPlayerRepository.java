package com.example.fantasy.repository;

import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.domain.FantasyTeamPlayer;
import com.example.fantasy.domain.BasketballPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FantasyTeamPlayerRepository extends JpaRepository<FantasyTeamPlayer, Long> {
    List<FantasyTeamPlayer> findByFantasyTeamAndActiveTrue(FantasyTeam team);
    Optional<FantasyTeamPlayer> findByFantasyTeamAndPlayerAndActiveTrue(FantasyTeam team, BasketballPlayer player);

    @Query("select count(distinct tp.fantasyTeam.id) from FantasyTeamPlayer tp where tp.active = true and tp.player.id = :playerId")
    long countActiveTeamsByPlayerId(@Param("playerId") Long playerId);
}
