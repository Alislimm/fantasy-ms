package com.example.fantasy.repository;

import com.example.fantasy.domain.Match;
import com.example.fantasy.domain.PlayerPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerPerformanceRepository extends JpaRepository<PlayerPerformance, Long> {
    List<PlayerPerformance> findByMatchIn(List<Match> matches);
}
