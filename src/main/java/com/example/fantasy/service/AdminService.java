package com.example.fantasy.service;

import com.example.fantasy.domain.BasketballPlayer;
import com.example.fantasy.domain.BasketballTeam;
import com.example.fantasy.domain.enums.PlayerPosition;
import com.example.fantasy.dto.AdminDtos;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.repository.BasketballPlayerRepository;
import com.example.fantasy.repository.BasketballTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class AdminService {

    private final BasketballTeamRepository teamRepo;
    private final BasketballPlayerRepository playerRepo;

    public AdminService(BasketballTeamRepository teamRepo, BasketballPlayerRepository playerRepo) {
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
    }

    public BasketballTeam upsertTeam(Long id, AdminDtos.TeamUpsertRequest req) {
        BasketballTeam team = (id == null)
                ? new BasketballTeam()
                : teamRepo.findById(id).orElseThrow(() -> new NotFoundException("Team not found"));
        team.setName(req.name());
        team.setShortName(req.shortName());
        team.setCity(req.city());
        return teamRepo.save(team);
    }

    public void deleteTeam(Long id) {
        teamRepo.deleteById(id);
    }

    public BasketballPlayer upsertPlayer(Long id, AdminDtos.PlayerUpsertRequest req) {
        BasketballPlayer p = (id == null)
                ? new BasketballPlayer()
                : playerRepo.findById(id).orElseThrow(() -> new NotFoundException("Player not found"));
        BasketballTeam team = teamRepo.findById(req.teamId()).orElseThrow(() -> new NotFoundException("Team not found"));
        p.setFirstName(req.firstName());
        p.setLastName(req.lastName());
        p.setTeam(team);
        p.setPosition(PlayerPosition.valueOf(req.position()));
        p.setNationality(req.nationality());
        p.setMarketValue(req.marketValue() == null ? BigDecimal.ZERO : req.marketValue());
        p.setActive(req.active());
        return playerRepo.save(p);
    }

    public void deletePlayer(Long id) {
        playerRepo.deleteById(id);
    }
}
