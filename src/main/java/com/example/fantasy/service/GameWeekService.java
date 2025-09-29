package com.example.fantasy.service;

import com.example.fantasy.domain.*;
import com.example.fantasy.domain.enums.GameWeekStatus;
import com.example.fantasy.domain.enums.MatchStatus;
import com.example.fantasy.dto.FantasyDtos;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.exception.ValidationException;
import com.example.fantasy.repository.*;
import com.example.fantasy.util.ScoringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameWeekService {

    @PersistenceContext
    private EntityManager entityManager;

    private final GameWeekRepository gwRepo;
    private final MatchRepository matchRepo;
    private final PlayerPerformanceRepository perfRepo;
    private final ScoringRuleRepository scoringRepo;
    private final LineupRepository lineupRepo;
    private final LineupSlotRepository slotRepo;
    private final FantasyTeamRepository teamRepo;
    private final BasketballTeamRepository basketballTeamRepo;

    public GameWeekService(GameWeekRepository gwRepo, MatchRepository matchRepo, PlayerPerformanceRepository perfRepo,
                           ScoringRuleRepository scoringRepo, LineupRepository lineupRepo, LineupSlotRepository slotRepo,
                           FantasyTeamRepository teamRepo, BasketballTeamRepository basketballTeamRepo) {
        this.gwRepo = gwRepo;
        this.matchRepo = matchRepo;
        this.perfRepo = perfRepo;
        this.scoringRepo = scoringRepo;
        this.lineupRepo = lineupRepo;
        this.slotRepo = slotRepo;
        this.teamRepo = teamRepo;
        this.basketballTeamRepo = basketballTeamRepo;
    }

    public int calculateGameWeekPoints(Long gameWeekId) {
        GameWeek gw = gwRepo.findById(gameWeekId).orElseThrow(() -> new NotFoundException("GameWeek not found"));
        List<Match> matches = matchRepo.findByGameWeek(gw);
        Map<String, java.math.BigDecimal> ruleMap = ScoringUtil.toRuleMap(scoringRepo.findAll());

        // Precompute points per player in this GW
        Map<Long, Integer> playerPoints = new HashMap<>();
        perfRepo.findByMatchIn(matches).forEach(p -> {
            int fp = ScoringUtil.calculateFantasyPoints(p, ruleMap);
            playerPoints.put(p.getPlayer().getId(), fp);
        });

        // For each lineup, sum starter points (captain double if slotPosition == CPT); bench gets 0
        int totalUpdated = 0;
        for (Lineup l : lineupRepo.findAll()) {
            if (!l.getGameWeek().getId().equals(gw.getId())) continue;
            List<LineupSlot> slots = slotRepo.findByLineup(l);
            int sum = 0;
            for (LineupSlot s : slots) {
                if (Boolean.TRUE.equals(s.isStarter())) {
                    int p = playerPoints.getOrDefault(s.getPlayer().getId(), 0);
                    if ("CPT".equals(s.getSlotPosition())) {
                        p *= 2;
                    }
                    sum += p;
                }
            }
            FantasyTeam team = l.getFantasyTeam();
            team.setTotalPoints(team.getTotalPoints() + sum);
            teamRepo.save(team);
            entityManager.flush();
            totalUpdated += sum;
        }
        return totalUpdated;
    }

    public GameWeek createGameWeek(Integer number, LocalDate startDate, LocalDate endDate, GameWeekStatus status) {
        // Check if gameweek number already exists
        if (gwRepo.findByNumber(number).isPresent()) {
            throw new ValidationException("GameWeek with number " + number + " already exists");
        }

        GameWeek gameWeek = GameWeek.builder()
                .number(number)
                .startDate(startDate)
                .endDate(endDate)
                .status(status != null ? status : GameWeekStatus.UPCOMING)
                .build();

        GameWeek savedGameWeek = gwRepo.save(gameWeek);
        entityManager.flush();
        return savedGameWeek;
    }

    public GameWeek getGameWeek(Long id) {
        return gwRepo.findById(id).orElseThrow(() -> new NotFoundException("GameWeek not found"));
    }

    public List<GameWeek> getAllGameWeeks() {
        return gwRepo.findAll();
    }

    public Match addFixture(Long gameWeekId, Long homeTeamId, Long awayTeamId, Instant kickoff, String venue) {
        GameWeek gameWeek = gwRepo.findById(gameWeekId).orElseThrow(() -> new NotFoundException("GameWeek not found"));
        BasketballTeam homeTeam = basketballTeamRepo.findById(homeTeamId).orElseThrow(() -> new NotFoundException("Home team not found"));
        BasketballTeam awayTeam = basketballTeamRepo.findById(awayTeamId).orElseThrow(() -> new NotFoundException("Away team not found"));

        if (homeTeamId.equals(awayTeamId)) {
            throw new ValidationException("Home team and away team cannot be the same");
        }

        Match match = Match.builder()
                .gameWeek(gameWeek)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .kickoff(kickoff)
                .venue(venue)
                .status(MatchStatus.SCHEDULED)
                .build();

        Match savedMatch = matchRepo.save(match);
        entityManager.flush();
        return savedMatch;
    }

    public List<Match> getGameWeekFixtures(Long gameWeekId) {
        GameWeek gameWeek = gwRepo.findById(gameWeekId).orElseThrow(() -> new NotFoundException("GameWeek not found"));
        return matchRepo.findByGameWeek(gameWeek);
    }

    public GameWeek initializeGameWeekOne() {
        // Check if gameweek 1 already exists
        Optional<GameWeek> existingGW = gwRepo.findByNumber(1);
        if (existingGW.isPresent()) {
            return existingGW.get();
        }

        // Create gameweek 1
        GameWeek gameWeek = createGameWeek(1, 
            LocalDate.now().plusDays(1), 
            LocalDate.now().plusDays(7), 
            GameWeekStatus.UPCOMING);

        // Get teams with IDs 1-10
        List<BasketballTeam> teams = basketballTeamRepo.findAllById(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        
        if (teams.size() < 10) {
            throw new ValidationException("Not enough teams available. Found " + teams.size() + " teams, need 10.");
        }

        // Generate 5 random matches
        Random random = new Random();
        Set<Long> usedTeams = new HashSet<>();
        List<Match> matches = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            BasketballTeam homeTeam, awayTeam;
            
            // Find two teams that haven't been used yet
            do {
                homeTeam = teams.get(random.nextInt(teams.size()));
            } while (usedTeams.contains(homeTeam.getId()));
            
            do {
                awayTeam = teams.get(random.nextInt(teams.size()));
            } while (usedTeams.contains(awayTeam.getId()) || awayTeam.getId().equals(homeTeam.getId()));
            
            usedTeams.add(homeTeam.getId());
            usedTeams.add(awayTeam.getId());
            
            // Create match
            Match match = addFixture(gameWeek.getId(), 
                homeTeam.getId(), 
                awayTeam.getId(), 
                Instant.now().plusSeconds(86400 * (i + 1)), // Spread matches over different days
                "Stadium " + (i + 1));
            matches.add(match);
        }

        return gameWeek;
    }

    public List<String> getGameWeekFixturesFormatted(Long gameWeekId) {
        GameWeek gameWeek = gwRepo.findById(gameWeekId).orElseThrow(() -> new NotFoundException("GameWeek with ID " + gameWeekId + " not found"));
        List<Match> matches = matchRepo.findByGameWeek(gameWeek);
        
        return matches.stream()
            .map(match -> {
                try {
                    String homeName = match.getHomeTeam() != null ? match.getHomeTeam().getName() : "Unknown";
                    String awayName = match.getAwayTeam() != null ? match.getAwayTeam().getName() : "Unknown";
                    return "[" + homeName + "," + awayName + "]";
                } catch (Exception e) {
                    throw new RuntimeException("Error formatting match with ID " + match.getId(), e);
                }
            })
            .collect(Collectors.toList());
    }
}
