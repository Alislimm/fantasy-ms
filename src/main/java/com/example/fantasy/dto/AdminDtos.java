package com.example.fantasy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AdminDtos {
    public record TeamUpsertRequest(
            @NotBlank String name,
            String shortName,
            String city
    ) {}

    public record PlayerUpsertRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String position,
            @NotNull Long teamId,
            String nationality,
            BigDecimal marketValue,
            boolean active
    ) {}

    public record PlayerResponse(Long id, String firstName, String lastName, String position, Long teamId) {}
    public record TeamResponse(Long id, String name, String shortName, String city) {}
}
