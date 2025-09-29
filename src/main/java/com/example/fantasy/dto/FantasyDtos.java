package com.example.fantasy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FantasyDtos {
    public record FantasyTeamCreateRequest(
            @NotBlank String teamName,
            @NotNull Long ownerUserId
    ) {}

    public record FantasyTeamResponse(Long id, String teamName, int totalPoints, int transfersRemaining) {}

    public record TransferRequest(
            @NotNull Long fantasyTeamId,
            @NotNull Long gameWeekId,
            @NotNull Long playerOutId,
            @NotNull Long playerInId
    ) {}

    public record LineupSelectionRequest(
            @NotNull Long fantasyTeamId,
            @NotNull Long gameWeekId,
            @NotNull List<Long> starters, // 5 players
            @NotNull List<Long> bench,    // 3 players
            Long captainPlayerId // optional, must be among starters
    ) {}

    public record InitialSquadBuildRequest(
            @NotNull Long fantasyTeamId,
            @NotNull List<Long> playerIds // 8 players for initial squad
    ) {}

    public record SquadBuildRequest(
            @NotBlank String teamName,
            @NotNull Long ownerUserId,
            @NotNull List<Long> starters, // 5 players
            @NotNull List<Long> bench,    // 3 players
            @NotNull Long captainPlayerId,
            @NotNull Long viceCaptainPlayerId
    ) {}
}
