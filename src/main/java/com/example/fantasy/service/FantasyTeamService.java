package com.example.fantasy.service;

import com.example.fantasy.domain.*;
import com.example.fantasy.dto.FantasyDtos;
import com.example.fantasy.exception.NotFoundException;
import com.example.fantasy.exception.ValidationException;
import com.example.fantasy.repository.*;
import com.example.fantasy.util.LineupUtil;
import com.example.fantasy.util.TransferUtil;
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

        // Update user's hasFantasyTeam flag
        owner.setHasFantasyTeam(true);
        userService.save(owner);

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
        Set<Long> activePlayerIds = active.stream().map(tp -> tp.getPlayer().getId()).collect(java.util.stream.Collectors.toSet());
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
}
