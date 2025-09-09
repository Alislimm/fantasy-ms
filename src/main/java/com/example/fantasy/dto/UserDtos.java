package com.example.fantasy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDtos {
    public record UserRegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password,
            Long favouriteTeamId,
            String nationality
    ) {}

    public record LoginRequest(
            @NotBlank String usernameOrEmail,
            @NotBlank String password
    ) {}

    public record UserResponse(Long id, String username, String email) {}
}
