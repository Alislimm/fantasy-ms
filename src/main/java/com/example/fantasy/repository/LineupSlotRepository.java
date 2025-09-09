package com.example.fantasy.repository;

import com.example.fantasy.domain.Lineup;
import com.example.fantasy.domain.LineupSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineupSlotRepository extends JpaRepository<LineupSlot, Long> {
    List<LineupSlot> findByLineup(Lineup lineup);
}
