package com.example.fantasy.service;

import com.example.fantasy.domain.FantasyTeam;
import com.example.fantasy.domain.Transfer;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.repository.FantasyLeagueRepository;
import com.example.fantasy.repository.FantasyLeagueTeamRepository;
import com.example.fantasy.repository.FantasyTeamRepository;
import com.example.fantasy.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LeaderboardService {

    private final FantasyTeamRepository fantasyTeamRepo;
    private final FantasyLeagueRepository fantasyLeagueRepo;
    private final FantasyLeagueTeamRepository fantasyLeagueTeamRepo;
    private final TransferRepository transferRepo;

    public LeaderboardService(FantasyTeamRepository fantasyTeamRepo,
                              FantasyLeagueRepository fantasyLeagueRepo,
                              FantasyLeagueTeamRepository fantasyLeagueTeamRepo,
                              TransferRepository transferRepo) {
        this.fantasyTeamRepo = fantasyTeamRepo;
        this.fantasyLeagueRepo = fantasyLeagueRepo;
        this.fantasyLeagueTeamRepo = fantasyLeagueTeamRepo;
        this.transferRepo = transferRepo;
    }

    public List<LeaderboardEntry> getLeagueLeaderboard(Long leagueId) {
        // Verify league exists
        fantasyLeagueRepo.findById(leagueId)
                .orElseThrow(() -> new NotFoundException("League not found"));

        // Get all teams in this league
        List<Long> teamIds = fantasyLeagueTeamRepo.findTeamIdsByLeagueId(leagueId);
        List<FantasyTeam> teams = fantasyTeamRepo.findAllById(teamIds);

        return buildLeaderboard(teams);
    }

    public List<LeaderboardEntry> getGlobalLeaderboard() {
        // Get all fantasy teams
        List<FantasyTeam> teams = fantasyTeamRepo.findAll();
        return buildLeaderboard(teams);
    }

    private List<LeaderboardEntry> buildLeaderboard(List<FantasyTeam> teams) {
        return teams.stream()
                .map(this::createLeaderboardEntry)
                .sorted(getLeaderboardComparator())
                .toList();
    }

    private LeaderboardEntry createLeaderboardEntry(FantasyTeam team) {
        // Count total transfers made by this team
        long totalTransfers = transferRepo.countByFantasyTeamId(team.getId());
        
        // Get team creation time (registration time)
        Instant registrationTime = team.getCreatedAt() != null ? team.getCreatedAt() : Instant.now();

        return new LeaderboardEntry(
                team.getId(),
                team.getTeamName(),
                team.getOwner().getUsername(),
                team.getTotalPoints(),
                totalTransfers,
                registrationTime
        );
    }

    private Comparator<LeaderboardEntry> getLeaderboardComparator() {
        return Comparator
                .comparingInt(LeaderboardEntry::totalPoints).reversed() // Higher points first
                .thenComparingLong(LeaderboardEntry::totalTransfers) // Fewer transfers first (tie-breaker)
                .thenComparing(LeaderboardEntry::registrationTime); // Earlier registration first (tie-breaker)
    }

    public record LeaderboardEntry(
            Long teamId,
            String teamName,
            String ownerUsername,
            int totalPoints,
            long totalTransfers,
            Instant registrationTime
    ) {}
}