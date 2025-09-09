package com.example.fantasy.repository;

import com.example.fantasy.domain.GameWeek;
import com.example.fantasy.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByGameWeek(GameWeek gameWeek);
}
