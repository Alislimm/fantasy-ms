package com.example.fantasy.repository;

import com.example.fantasy.domain.BasketballTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BasketballTeamRepository extends JpaRepository<BasketballTeam, Long> {
    Optional<BasketballTeam> findByName(String name);
}
