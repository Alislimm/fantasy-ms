package com.example.fantasy.repository;

import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FantasyTeamRepository extends JpaRepository<FantasyTeam, Long> {
    List<FantasyTeam> findByOwner(User owner);
}
