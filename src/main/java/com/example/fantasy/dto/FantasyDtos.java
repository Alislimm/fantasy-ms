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
}
