package com.example.fantasy.controller;

import com.example.fantasy.domain.Notification;
import com.example.fantasy.repository.NotificationRepository;
import com.example.fantasy.security.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepo;

    public NotificationController(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long userId = getUserIdFromToken(request);
        
        if (unreadOnly) {
            List<Notification> notifications = notificationRepo.findByUserIdAndReadStatusOrderByTimestampDesc(userId, false);
            return ResponseEntity.ok(notifications);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notificationPage = notificationRepo.findByUserIdOrderByTimestampDesc(userId, pageable);
            return ResponseEntity.ok(notificationPage.getContent());
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getNotificationCounts(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        
        long totalCount = notificationRepo.countByUserIdAndReadStatus(userId, null);
        long unreadCount = notificationRepo.countByUserIdAndReadStatus(userId, false);
        
        Map<String, Long> counts = Map.of(
                "total", totalCount,
                "unread", unreadCount
        );
        
        return ResponseEntity.ok(counts);
    }

    @PostMapping("/{notificationId}/read")
    @Transactional
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long notificationId,
            HttpServletRequest request) {
        
        Long userId = getUserIdFromToken(request);
        notificationRepo.markAsReadByUserAndId(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    @Transactional
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        notificationRepo.markAllAsReadForUser(userId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            var claims = JwtUtil.parse(token);
            return claims.get("uid", Long.class);
        }
        throw new RuntimeException("No valid token found");
    }
}