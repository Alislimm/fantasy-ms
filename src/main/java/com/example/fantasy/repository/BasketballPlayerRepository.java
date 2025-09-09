package com.example.fantasy.repository;

import com.example.fantasy.domain.BasketballPlayer;
import com.example.fantasy.domain.enums.PlayerPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BasketballPlayerRepository extends JpaRepository<BasketballPlayer, Long> {
    List<BasketballPlayer> findByPosition(PlayerPosition position);
    List<BasketballPlayer> findByActiveTrue();
}
