package com.example.fantasy.service;

import com.example.fantasy.domain.*;
import com.example.fantasy.dto.FantasyDtos;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.exception.ValidationException;
import com.example.fantasy.repository.*;
import com.example.fantasy.util.LineupUtil;
import com.example.fantasy.util.TransferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class FantasyTeamService {

    private static final Logger logger = LoggerFactory.getLogger(FantasyTeamService.class);

    private final FantasyTeamRepository teamRepo;
    private final UserService userService;
    private final BasketballPlayerRepository playerRepo;
    private final GameWeekRepository gameWeekRepo;
    private final FantasyTeamPlayerRepository teamPlayerRepo;
    private final LineupRepository lineupRepo;
    private final LineupSlotRepository slotRepo;
    private final TransferRepository transferRepo;
    private final FantasyLeagueRepository leagueRepo;
    private final FantasyLeagueTeamRepository leagueTeamRepo;

    public FantasyTeamService(FantasyTeamRepository teamRepo,
                              UserService userService,
                              BasketballPlayerRepository playerRepo,
                              GameWeekRepository gameWeekRepo,
                              FantasyTeamPlayerRepository teamPlayerRepo,
                              LineupRepository lineupRepo,
                              LineupSlotRepository slotRepo,
                              TransferRepository transferRepo,
                              FantasyLeagueRepository leagueRepo,
                              FantasyLeagueTeamRepository leagueTeamRepo) {
        this.teamRepo = teamRepo;
        this.userService = userService;
        this.playerRepo = playerRepo;
        this.gameWeekRepo = gameWeekRepo;
        this.teamPlayerRepo = teamPlayerRepo;
        this.lineupRepo = lineupRepo;
        this.slotRepo = slotRepo;
        this.transferRepo = transferRepo;
        this.leagueRepo = leagueRepo;
        this.leagueTeamRepo = leagueTeamRepo;
    }

    public FantasyTeam createTeam(FantasyDtos.FantasyTeamCreateRequest req) {
        User owner = userService.getUser(req.ownerUserId());
        
        // Check if user already has a fantasy team
        if (owner.isHasFantasyTeam()) {
            throw new ValidationException("User already has a fantasy team. Each user can only have one fantasy team.");
        }
        
        FantasyTeam t = new FantasyTeam();
        t.setOwner(owner);
        t.setTeamName(req.teamName());
        t.setBudget(new BigDecimal("100.00"));
        t.setTotalPoints(0);
        t.setTransfersRemaining(1);
        t = teamRepo.save(t);

        // Note: hasFantasyTeam flag will be set only after successful squad build

        // Auto-join leagues: overall, favourite team, nationality
        ensureAndJoinLeagues(owner, t);
        return t;
    }

    private void ensureAndJoinLeagues(User owner, FantasyTeam team) {
        // Overall League
        com.example.fantasy.domain.FantasyLeague overall = leagueRepo.findByName("Overall League")
                .orElseGet(() -> {
                    com.example.fantasy.domain.FantasyLeague l = new com.example.fantasy.domain.FantasyLeague();
                    l.setName("Overall League");
                    l.setType(com.example.fantasy.domain.enums.LeagueType.PUBLIC);
                    l.setCreatedBy(owner);
                    return leagueRepo.save(l);
                });
        joinIfNotJoined(overall, team);

        // Favourite Team League
        if (owner.getFavouriteTeam() != null && owner.getFavouriteTeam().getId() != null) {
            Long tid = owner.getFavouriteTeam().getId();
            String lname = "Team League - TEAM_" + tid;
            com.example.fantasy.domain.FantasyLeague teamLeague = leagueRepo.findByName(lname)
                    .orElseGet(() -> {
                        com.example.fantasy.domain.FantasyLeague l = new com.example.fantasy.domain.FantasyLeague();
                        l.setName(lname);
                        l.setType(com.example.fantasy.domain.enums.LeagueType.PUBLIC);
                        l.setCreatedBy(owner);
                        return leagueRepo.save(l);
                    });
            joinIfNotJoined(teamLeague, team);
        }

        // Nationality League
        if (owner.getNationality() != null && !owner.getNationality().isBlank()) {
            String natKey = owner.getNationality().trim().toUpperCase();
            String lname = "Nation League - " + natKey;
            com.example.fantasy.domain.FantasyLeague natLeague = leagueRepo.findByName(lname)
                    .orElseGet(() -> {
                        com.example.fantasy.domain.FantasyLeague l = new com.example.fantasy.domain.FantasyLeague();
                        l.setName(lname);
                        l.setType(com.example.fantasy.domain.enums.LeagueType.PUBLIC);
                        l.setCreatedBy(owner);
                        return leagueRepo.save(l);
                    });
            joinIfNotJoined(natLeague, team);
        }
    }

    private void joinIfNotJoined(com.example.fantasy.domain.FantasyLeague league, FantasyTeam team) {
        if (leagueTeamRepo.findByLeagueAndTeam(league, team).isEmpty()) {
            com.example.fantasy.domain.FantasyLeagueTeam link = new com.example.fantasy.domain.FantasyLeagueTeam();
            link.setLeague(league);
            link.setTeam(team);
            link.setTotalPoints(0);
            link.setRank(null);
            link.setJoinedAt(java.time.Instant.now());
            leagueTeamRepo.save(link);
        }
    }

    public Lineup setLineup(FantasyDtos.LineupSelectionRequest req) {
        FantasyTeam team = teamRepo.findById(req.fantasyTeamId()).orElseThrow(() -> new NotFoundException("Team not found"));
        GameWeek gw = gameWeekRepo.findById(req.gameWeekId()).orElseThrow(() -> new NotFoundException("GameWeek not found"));

        LineupUtil.validateComposition(req.starters(), req.bench());
        // Deadline rule: only allow lineup changes if GW is UPCOMING or ACTIVE and kickoff not passed (simplified)
        if (gw.getStatus() == com.example.fantasy.domain.enums.GameWeekStatus.COMPLETED) {
            throw new ValidationException("Cannot set lineup for completed GameWeek");
        }

        Lineup lineup = lineupRepo.findByFantasyTeamAndGameWeek(team, gw).orElseGet(() -> {
            Lineup l = new Lineup();
            l.setFantasyTeam(team);
            l.setGameWeek(gw);
            l.setCreatedAt(Instant.now());
            return lineupRepo.save(l);
        });

        // remove old slots
        slotRepo.deleteAll(slotRepo.findByLineup(lineup));

        Set<Long> all = new HashSet<>();
        all.addAll(req.starters());
        all.addAll(req.bench());

        // Ensure players are in team squad (active)
        List<FantasyTeamPlayer> active = teamPlayerRepo.findByFantasyTeamAndActiveTrue(team);
        Set<Long> activePlayerIds = active.stream()
                .filter(tp -> tp.getPlayer() != null && tp.getPlayer().getId() != null)
                .map(tp -> tp.getPlayer().getId())
                .collect(java.util.stream.Collectors.toSet());
        for (Long pid : all) {
            if (!activePlayerIds.contains(pid)) {
                throw new ValidationException("Player " + pid + " not in team squad");
            }
        }

        // create slots
        for (Long pid : req.starters()) {
            LineupSlot s = new LineupSlot();
            s.setLineup(lineup);
            s.setPlayer(playerRepo.findById(pid).orElseThrow(() -> new NotFoundException("Player not found")));
            s.setStarter(true);
            s.setSlotPosition(null);
            slotRepo.save(s);
        }
        for (Long pid : req.bench()) {
            LineupSlot s = new LineupSlot();
            s.setLineup(lineup);
            s.setPlayer(playerRepo.findById(pid).orElseThrow(() -> new NotFoundException("Player not found")));
            s.setStarter(false);
            s.setSlotPosition(null);
            slotRepo.save(s);
        }

        // Set captain if provided
        if (req.captainPlayerId() != null) {
            if (!req.starters().contains(req.captainPlayerId())) {
                throw new ValidationException("Captain must be among starters");
            }
            List<LineupSlot> slots = slotRepo.findByLineup(lineup);
            for (LineupSlot s : slots) {
                if (s.isStarter() && s.getPlayer().getId().equals(req.captainPlayerId())) {
                    s.setSlotPosition("CPT");
                } else if ("CPT".equals(s.getSlotPosition())) {
                    s.setSlotPosition(null);
                }
            }
            slotRepo.saveAll(slots);
        }

        return lineup;
    }

    public Transfer makeTransfer(FantasyDtos.TransferRequest req) {
        FantasyTeam team = teamRepo.findById(req.fantasyTeamId()).orElseThrow(() -> new NotFoundException("Team not found"));
        GameWeek gw = gameWeekRepo.findById(req.gameWeekId()).orElseThrow(() -> new NotFoundException("GameWeek not found"));
        BasketballPlayer out = playerRepo.findById(req.playerOutId()).orElseThrow(() -> new NotFoundException("Player out not found"));
        BasketballPlayer in = playerRepo.findById(req.playerInId()).orElseThrow(() -> new NotFoundException("Player in not found"));

        // ensure 'out' is in team and active; ensure 'in' not already in team active
        // todo : check commented code from repo
        FantasyTeamPlayer outLink = teamPlayerRepo.findByFantasyTeamAndPlayerAndActiveTrue(team, out)
                .orElseThrow(() -> new ValidationException("Player out not in active squad"));
        if (teamPlayerRepo.findByFantasyTeamAndPlayerAndActiveTrue(team, in).isPresent()) {
            throw new ValidationException("Player in already in squad");
        }

        // Deadline: transfers only when GW is UPCOMING or ACTIVE
        if (gw.getStatus() == com.example.fantasy.domain.enums.GameWeekStatus.COMPLETED) {
            throw new ValidationException("Transfers closed for completed GameWeek");
        }

        // deactivate out
        outLink.setActive(false);
        outLink.setReleasedAt(Instant.now());
        teamPlayerRepo.save(outLink);

        // add in
        FantasyTeamPlayer inLink = new FantasyTeamPlayer();
        inLink.setFantasyTeam(team);
        inLink.setPlayer(in);
        inLink.setActive(true);
        inLink.setAcquiredAt(Instant.now());
        inLink.setPurchasePrice(in.getMarketValue() == null ? BigDecimal.ZERO : in.getMarketValue());
        teamPlayerRepo.save(inLink);

        // record transfer
        Transfer transfer = new Transfer();
        transfer.setFantasyTeam(team);
        transfer.setGameWeek(gw);
        transfer.setPlayerOut(out);
        transfer.setPlayerIn(in);
        transfer.setPriceDifference((in.getMarketValue() == null ? BigDecimal.ZERO : in.getMarketValue())
                .subtract(out.getMarketValue() == null ? BigDecimal.ZERO : out.getMarketValue()));
        transfer.setCreatedAt(Instant.now());
        transferRepo.save(transfer);

        // apply penalty if needed
        long count = transferRepo.countByFantasyTeamAndGameWeek(team, gw);
        int penalty = TransferUtil.calculateTransferPenalty(count);
        if (penalty > 0) {
            team.setTotalPoints(Math.max(0, team.getTotalPoints() - penalty));
            teamRepo.save(team);
        }

        return transfer;
    }

    public FantasyTeam buildInitialSquad(FantasyDtos.InitialSquadBuildRequest req) {
        logger.info("[SQUAD_BUILD] Starting initial squad build for fantasyTeamId: {}, playerIds: {}", 
                    req.fantasyTeamId(), req.playerIds());
        
        try {
            // Find team
            logger.debug("[SQUAD_BUILD] Looking up team with ID: {}", req.fantasyTeamId());
            FantasyTeam team = teamRepo.findById(req.fantasyTeamId())
                    .orElseThrow(() -> new NotFoundException("Team not found"));
            logger.debug("[SQUAD_BUILD] Found team: {} (Owner: {})", team.getTeamName(), team.getOwner().getUsername());
            
            // Ensure team has empty squad
            logger.debug("[SQUAD_BUILD] Checking existing squad for team: {}", req.fantasyTeamId());
            List<FantasyTeamPlayer> existingPlayers = teamPlayerRepo.findByFantasyTeamAndActiveTrue(team);
            logger.debug("[SQUAD_BUILD] Found {} existing active players in squad", existingPlayers.size());
            if (!existingPlayers.isEmpty()) {
                logger.warn("[SQUAD_BUILD] Team {} already has {} players in squad, rejecting request", 
                           req.fantasyTeamId(), existingPlayers.size());
                throw new ValidationException("Team already has players in squad. Initial squad building only allowed for empty teams.");
            }
            
            // Validate player count (should be 8 for full squad)
            logger.debug("[SQUAD_BUILD] Validating player count: {} (expected: 8)", req.playerIds().size());
            if (req.playerIds().size() != 8) {
                logger.warn("[SQUAD_BUILD] Invalid player count: {} (expected: 8)", req.playerIds().size());
                throw new ValidationException("Initial squad must contain exactly 8 players");
            }
            
            // Validate all players exist and calculate total cost
            logger.debug("[SQUAD_BUILD] Validating players and calculating total cost");
            BigDecimal totalCost = BigDecimal.ZERO;
            for (Long playerId : req.playerIds()) {
                logger.debug("[SQUAD_BUILD] Validating player ID: {}", playerId);
                BasketballPlayer player = playerRepo.findById(playerId)
                        .orElseThrow(() -> new NotFoundException("Player not found: " + playerId));
                
                if (!player.isActive()) {
                    logger.warn("[SQUAD_BUILD] Player {} is not active", playerId);
                    throw new ValidationException("Player " + playerId + " is not active");
                }
                
                BigDecimal playerCost = player.getMarketValue() == null ? BigDecimal.ZERO : player.getMarketValue();
                totalCost = totalCost.add(playerCost);
                logger.debug("[SQUAD_BUILD] Player {} ({} {}) cost: {}, running total: {}", 
                           playerId, player.getFirstName(), player.getLastName(), playerCost, totalCost);
            }
            
            // Check budget
            logger.debug("[SQUAD_BUILD] Checking budget: totalCost={}, teamBudget={}", totalCost, team.getBudget());
            if (totalCost.compareTo(team.getBudget()) > 0) {
                logger.warn("[SQUAD_BUILD] Budget exceeded: totalCost={}, teamBudget={}", totalCost, team.getBudget());
                throw new ValidationException("Total player cost (" + totalCost + ") exceeds team budget (" + team.getBudget() + ")");
            }
            
            // Create FantasyTeamPlayer records for all players
            logger.debug("[SQUAD_BUILD] Creating FantasyTeamPlayer records for {} players", req.playerIds().size());
            for (Long playerId : req.playerIds()) {
                logger.debug("[SQUAD_BUILD] Creating FantasyTeamPlayer record for player: {}", playerId);
                BasketballPlayer player = playerRepo.findById(playerId).get(); // Already validated above
                
                FantasyTeamPlayer teamPlayer = new FantasyTeamPlayer();
                teamPlayer.setFantasyTeam(team);
                teamPlayer.setPlayer(player);
                teamPlayer.setActive(true);
                teamPlayer.setAcquiredAt(Instant.now());
                teamPlayer.setPurchasePrice(player.getMarketValue() == null ? BigDecimal.ZERO : player.getMarketValue());
                
                try {
                    teamPlayerRepo.save(teamPlayer);
                    logger.debug("[SQUAD_BUILD] Successfully saved FantasyTeamPlayer for player: {}", playerId);
                } catch (Exception e) {
                    logger.error("[SQUAD_BUILD] Failed to save FantasyTeamPlayer for player: {}", playerId, e);
                    throw e;
                }
            }
            
            // Update team budget
            logger.debug("[SQUAD_BUILD] Updating team budget from {} to {}", 
                        team.getBudget(), team.getBudget().subtract(totalCost));
            team.setBudget(team.getBudget().subtract(totalCost));
            
            try {
                teamRepo.save(team);
                logger.debug("[SQUAD_BUILD] Successfully updated team budget");
            } catch (Exception e) {
                logger.error("[SQUAD_BUILD] Failed to update team budget", e);
                throw e;
            }
            
            logger.info("[SQUAD_BUILD] Successfully completed initial squad build for team: {} with {} players, total cost: {}", 
                       team.getTeamName(), req.playerIds().size(), totalCost);
            return team;
            
        } catch (Exception e) {
            logger.error("[SQUAD_BUILD] Error during initial squad build for fantasyTeamId: {}, playerIds: {}", 
                        req.fantasyTeamId(), req.playerIds(), e);
            throw e;
        }
    }

    public FantasyTeam buildSquad(FantasyDtos.SquadBuildRequest req) {
        logger.info("[SQUAD_BUILD] Starting squad build for teamName: {}, ownerUserId: {}", 
                    req.teamName(), req.ownerUserId());
        
        try {
            // First create the fantasy team
            FantasyDtos.FantasyTeamCreateRequest createRequest = 
                new FantasyDtos.FantasyTeamCreateRequest(req.teamName(), req.ownerUserId());
            FantasyTeam team = createTeam(createRequest);
            
            // Combine starters and bench into playerIds list
            List<Long> allPlayerIds = new java.util.ArrayList<>();
            allPlayerIds.addAll(req.starters());
            allPlayerIds.addAll(req.bench());
            
            // Build the initial squad with all players
            FantasyDtos.InitialSquadBuildRequest squadRequest = 
                new FantasyDtos.InitialSquadBuildRequest(team.getId(), allPlayerIds);
            team = buildInitialSquad(squadRequest);
            
            // Only set hasFantasyTeam flag after successful squad build
            User owner = team.getOwner();
            owner.setHasFantasyTeam(true);
            userService.save(owner);
            
            logger.info("[SQUAD_BUILD] Successfully completed squad build for team: {} with captain: {}, vice-captain: {}", 
                       team.getTeamName(), req.captainPlayerId(), req.viceCaptainPlayerId());
            return team;
            
        } catch (Exception e) {
            logger.error("[SQUAD_BUILD] Error during squad build for teamName: {}, ownerUserId: {}", 
                        req.teamName(), req.ownerUserId(), e);
            throw e;
        }
    }

    public FantasyTeam getFantasyTeamByUserId(Long userId) {
        User user = userService.getUser(userId);
        
        // Check if user has a fantasy team
        if (!user.isHasFantasyTeam()) {
            throw new NotFoundException("User does not have a fantasy team");
        }
        
        // Find the user's fantasy team
        List<FantasyTeam> teams = teamRepo.findByOwner(user);
        if (teams.isEmpty()) {
            throw new NotFoundException("Fantasy team not found for user");
        }
        
        // Return the first (and should be only) fantasy team for this user
        return teams.get(0);
    }
}
