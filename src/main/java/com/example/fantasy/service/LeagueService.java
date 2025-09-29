package com.example.fantasy.service;

import com.example.fantasy.controller.LeagueController;
import com.example.fantasy.domain.FantasyLeague;
import com.example.fantasy.domain.FantasyLeagueTeam;
import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.domain.User;
import com.example.fantasy.domain.enums.LeagueType;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.exception.ValidationException;
import com.example.fantasy.repository.FantasyLeagueRepository;
import com.example.fantasy.repository.FantasyLeagueTeamRepository;
import com.example.fantasy.repository.FantasyTeamRepository;
import com.example.fantasy.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeagueService {

    private final FantasyLeagueRepository leagueRepo;
    private final UserRepository userRepo;
    private final FantasyTeamRepository teamRepo;
    private final FantasyLeagueTeamRepository leagueTeamRepo;
    private final Random random = new Random();

    public LeagueService(FantasyLeagueRepository leagueRepo, UserRepository userRepo, 
                        FantasyTeamRepository teamRepo, FantasyLeagueTeamRepository leagueTeamRepo) {
        this.leagueRepo = leagueRepo;
        this.userRepo = userRepo;
        this.teamRepo = teamRepo;
        this.leagueTeamRepo = leagueTeamRepo;
    }

    public FantasyLeague createLeague(Long userId, LeagueController.CreateLeagueRequest request) {
        // Get user by ID
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Create the league
        FantasyLeague league = FantasyLeague.builder()
                .name(request.name)
                .type(LeagueType.PRIVATE)
                .createdBy(user)
                .build();

        // Generate invite code for all leagues
        league.setInviteCode(generateInviteCode());

        // Generate 5-digit join code for private leagues
        if (league.getType() == LeagueType.PRIVATE) {
            league.setJoinCode(generateUniqueJoinCode());
        }

        // Save the league first
        league = leagueRepo.save(league);
        
        // Automatically join the league creator
        autoJoinCreator(league, user);

        return league;
    }

    private void autoJoinCreator(FantasyLeague league, User creator) {
        // Find creator's fantasy team
        List<FantasyTeam> userTeams = teamRepo.findByOwner(creator);
        if (userTeams.isEmpty()) {
            throw new ValidationException("Creator must have a fantasy team before creating a league");
        }
        FantasyTeam creatorTeam = userTeams.get(0); // Use the first team
        
        // Check if already joined (shouldn't happen, but safety check)
        if (leagueTeamRepo.findByLeagueAndTeam(league, creatorTeam).isPresent()) {
            return; // Already joined, nothing to do
        }
        
        // Create league membership for creator
        FantasyLeagueTeam link = new FantasyLeagueTeam();
        link.setLeague(league);
        link.setTeam(creatorTeam);
        link.setTotalPoints(0);
        link.setRank(null);
        link.setJoinedAt(Instant.now());
        leagueTeamRepo.save(link);
    }

    public void joinLeagueByCode(String joinCode, Long userId) {
        // Find the league by join code
        FantasyLeague league = leagueRepo.findByJoinCode(joinCode)
                .orElseThrow(() -> new NotFoundException("League not found with join code: " + joinCode));
        
        // Find the user
        User user = userRepo.findById(userId)
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

    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    private String generateUniqueJoinCode() {
        String joinCode;
        int attempts = 0;
        do {
            joinCode = String.format("%05d", random.nextInt(100000)); // Generate 5-digit code
            attempts++;
            if (attempts > 100) {
                throw new ValidationException("Unable to generate unique join code, please try again");
            }
        } while (leagueRepo.findByJoinCode(joinCode).isPresent());
        
        return joinCode;
    }

    public List<UserLeagueInfo> getUserLeagues(Long userId) {
        // Find the user
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Find user's fantasy team
        List<FantasyTeam> userTeams = teamRepo.findByOwner(user);
        if (userTeams.isEmpty()) {
            return new ArrayList<>(); // Return empty list if user has no fantasy team
        }
        FantasyTeam userTeam = userTeams.get(0); // Use the first team
        
        // Find all league memberships for this team
        List<FantasyLeagueTeam> memberships = leagueTeamRepo.findByTeam(userTeam);
        
        return memberships.stream()
                .map(membership -> new UserLeagueInfo(
                    membership.getLeague().getId(),
                    membership.getLeague().getName(),
                    membership.getLeague().getType().toString(),
                    membership.getTotalPoints(),
                    membership.getRank(),
                    membership.getJoinedAt()
                ))
                .collect(Collectors.toList());
    }

    public LeagueDetailsInfo getLeagueDetails(Long leagueId) {
        // Find the league
        FantasyLeague league = leagueRepo.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("League not found"));
        
        // Find all teams in the league ordered by total points (highest first)
        List<FantasyLeagueTeam> rankings = leagueTeamRepo.findByLeagueOrderByTotalPointsDesc(league);
        
        List<TeamRankingInfo> teamRankings = rankings.stream()
                .map(membership -> new TeamRankingInfo(
                    membership.getTeam().getTeamName(),
                    membership.getTotalPoints()
                ))
                .collect(Collectors.toList());
        
        return new LeagueDetailsInfo(
            league.getId(),
            league.getName(),
            league.getType().toString(),
            teamRankings
        );
    }

    public static class LeagueDetailsInfo {
        public Long leagueId;
        public String leagueName;
        public String leagueType;
        public List<TeamRankingInfo> rankings;

        public LeagueDetailsInfo(Long leagueId, String leagueName, String leagueType, 
                                List<TeamRankingInfo> rankings) {
            this.leagueId = leagueId;
            this.leagueName = leagueName;
            this.leagueType = leagueType;
            this.rankings = rankings;
        }
    }

    public static class TeamRankingInfo {
        public String teamName;
        public Integer totalPoints;

        public TeamRankingInfo(String teamName, Integer totalPoints) {
            this.teamName = teamName;
            this.totalPoints = totalPoints;
        }
    }

    public static class UserLeagueInfo {
        public Long leagueId;
        public String leagueName;
        public String leagueType;
        public Integer totalPoints;
        public Integer rank;
        public Instant joinedAt;

        public UserLeagueInfo(Long leagueId, String leagueName, String leagueType, 
                             Integer totalPoints, Integer rank, Instant joinedAt) {
            this.leagueId = leagueId;
            this.leagueName = leagueName;
            this.leagueType = leagueType;
            this.totalPoints = totalPoints;
            this.rank = rank;
            this.joinedAt = joinedAt;
        }
    }
}