package com.example.fantasy.controller;

import com.example.fantasy.domain.LeagueInvitation;
import com.example.fantasy.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<LeagueInvitation> create(@RequestParam Long leagueId,
                                                   @RequestParam Long invitedByUserId,
                                                   @RequestParam String email) {
        return ResponseEntity.ok(invitationService.createInvite(leagueId, invitedByUserId, email));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<LeagueInvitation> accept(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.respond(token, true));
    }

    @PostMapping("/{token}/decline")
    public ResponseEntity<LeagueInvitation> decline(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.respond(token, false));
    }
}