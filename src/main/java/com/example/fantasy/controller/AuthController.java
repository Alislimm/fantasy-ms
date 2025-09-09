package com.example.fantasy.controller;

import com.example.fantasy.domain.User;
import com.example.fantasy.dto.UserDtos;
import com.example.fantasy.security.JwtUtil;
import com.example.fantasy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Validated @RequestBody UserDtos.LoginRequest req) {
        User user = userService.login(req);
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("uid", user.getId());
        String token = JwtUtil.generateToken(user.getUsername(), claims);
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("userId", user.getId());
        body.put("username", user.getUsername());
        body.put("role", user.getRole());
        return ResponseEntity.ok(body);
    }
}