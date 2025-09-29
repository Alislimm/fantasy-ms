package com.example.fantasy.service;

import com.example.fantasy.domain.FantasyLeague;
import com.example.fantasy.domain.FantasyLeagueTeam;
import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.domain.LeagueInvitation;
import com.example.fantasy.domain.User;
import com.example.fantasy.domain.enums.InvitationStatus;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.exception.ValidationException;
import com.example.fantasy.repository.FantasyLeagueRepository;
import com.example.fantasy.repository.FantasyLeagueTeamRepository;
import com.example.fantasy.repository.FantasyTeamRepository;
import com.example.fantasy.repository.LeagueInvitationRepository;
import com.example.fantasy.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvitationService {

    private final LeagueInvitationRepository inviteRepo;
    private final FantasyLeagueRepository leagueRepo;
    private final UserRepository userRepo;
    private final FantasyTeamRepository teamRepo;
    private final FantasyLeagueTeamRepository leagueTeamRepo;

    public InvitationService(LeagueInvitationRepository inviteRepo, FantasyLeagueRepository leagueRepo, 
                           UserRepository userRepo, FantasyTeamRepository teamRepo, 
                           FantasyLeagueTeamRepository leagueTeamRepo) {
        this.inviteRepo = inviteRepo;
        this.leagueRepo = leagueRepo;
        this.userRepo = userRepo;
        this.teamRepo = teamRepo;
        this.leagueTeamRepo = leagueTeamRepo;
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

    public void joinLeagueByInviteCode(String inviteCode) {
        // Find the league by invite code
        FantasyLeague league = leagueRepo.findByInviteCode(inviteCode)
                .orElseThrow(() -> new NotFoundException("League not found with invite code: " + inviteCode));
        
        // Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(username)) {
            throw new ValidationException("You must be logged in to join a league. Please login or register first.");
        }
        
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Find user's fantasy team
        List<FantasyTeam> userTeams = teamRepo.findByOwner(user);
        if (userTeams.isEmpty()) {
            throw new ValidationException("User must create a fantasy team before joining a league");
        }
        FantasyTeam userTeam = userTeams.get(0); // Use the first team
        
        // Check if already joined
        if (leagueTeamRepo.findByLeagueAndTeam(league, userTeam).isPresent()) {
            throw new ValidationException("Already joined this league");
        }
        
        // Create league membership
        FantasyLeagueTeam link = new FantasyLeagueTeam();
        link.setLeague(league);
        link.setTeam(userTeam);
        link.setTotalPoints(0);
        link.setRank(null);
        link.setJoinedAt(Instant.now());
        leagueTeamRepo.save(link);
    }
}