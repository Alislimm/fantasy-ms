package com.example.fantasy.service;

import com.example.fantasy.domain.FantasyLeague;
import com.example.fantasy.domain.LeagueInvitation;
import com.example.fantasy.domain.User;
import com.example.fantasy.domain.enums.InvitationStatus;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.repository.FantasyLeagueRepository;
import com.example.fantasy.repository.LeagueInvitationRepository;
import com.example.fantasy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class InvitationService {

    private final LeagueInvitationRepository inviteRepo;
    private final FantasyLeagueRepository leagueRepo;
    private final UserRepository userRepo;

    public InvitationService(LeagueInvitationRepository inviteRepo, FantasyLeagueRepository leagueRepo, UserRepository userRepo) {
        this.inviteRepo = inviteRepo;
        this.leagueRepo = leagueRepo;
        this.userRepo = userRepo;
    }

    public LeagueInvitation createInvite(Long leagueId, Long invitedByUserId, String email) {
        FantasyLeague league = leagueRepo.findById(leagueId).orElseThrow(() -> new NotFoundException("League not found"));
        User inviter = userRepo.findById(invitedByUserId).orElseThrow(() -> new NotFoundException("User not found"));
        LeagueInvitation inv = new LeagueInvitation();
        inv.setLeague(league);
        inv.setInvitedBy(inviter);
        inv.setEmail(email);
        inv.setToken(UUID.randomUUID().toString().replace("-", ""));
        inv.setStatus(InvitationStatus.PENDING);
        inv.setCreatedAt(Instant.now());
        return inviteRepo.save(inv);
    }

    public LeagueInvitation respond(String token, boolean accept) {
        LeagueInvitation inv = inviteRepo.findByToken(token).orElseThrow(() -> new NotFoundException("Invitation not found"));
        inv.setStatus(accept ? InvitationStatus.ACCEPTED : InvitationStatus.DECLINED);
        inv.setRespondedAt(Instant.now());
        return inviteRepo.save(inv);
    }
}