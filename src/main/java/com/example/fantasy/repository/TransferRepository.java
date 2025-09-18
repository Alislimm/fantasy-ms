package com.example.fantasy.repository;

import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.domain.GameWeek;
import com.example.fantasy.domain.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    long countByFantasyTeamAndGameWeek(FantasyTeam team, GameWeek gw);
    long countByFantasyTeamId(Long fantasyTeamId);
}
