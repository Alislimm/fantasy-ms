package com.example.fantasy.repository;

import com.example.fantasy.domain.GameWeek;
import com.example.fantasy.domain.enums.GameWeekStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameWeekRepository extends JpaRepository<GameWeek, Long> {
    Optional<GameWeek> findByNumber(Integer number);
    Optional<GameWeek> findFirstByStatus(GameWeekStatus status);
}
