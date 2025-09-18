package com.example.fantasy.service;

import com.example.fantasy.domain.BasketballPlayer;
import com.example.fantasy.domain.GameWeek;
import com.example.fantasy.domain.PlayerPerformance;
import com.example.fantasy.domain.PlayerPriceHistory;
import com.example.fantasy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class PlayerPricingService {

    private final BasketballPlayerRepository playerRepo;
    private final PlayerPriceHistoryRepository priceHistoryRepo;
    private final PlayerPerformanceRepository performanceRepo;
    private final FantasyTeamPlayerRepository teamPlayerRepo;
    private final FantasyTeamRepository teamRepo;
    private final GameWeekRepository gameWeekRepo;

    // Pricing constants
    private static final BigDecimal PRICE_CHANGE_FACTOR = new BigDecimal("0.10"); // 10% max change
    private static final BigDecimal MIN_PRICE = new BigDecimal("4.0");
    private static final BigDecimal MAX_PRICE = new BigDecimal("15.0");
    private static final double HIGH_OWNERSHIP_THRESHOLD = 20.0; // 20%
    private static final double LOW_OWNERSHIP_THRESHOLD = 5.0;   // 5%
    private static final int HIGH_PERFORMANCE_THRESHOLD = 25; // fantasy points
    private static final int LOW_PERFORMANCE_THRESHOLD = 5;   // fantasy points

    public PlayerPricingService(BasketballPlayerRepository playerRepo,
                                PlayerPriceHistoryRepository priceHistoryRepo,
                                PlayerPerformanceRepository performanceRepo,
                                FantasyTeamPlayerRepository teamPlayerRepo,
                                FantasyTeamRepository teamRepo,
                                GameWeekRepository gameWeekRepo) {
        this.playerRepo = playerRepo;
        this.priceHistoryRepo = priceHistoryRepo;
        this.performanceRepo = performanceRepo;
        this.teamPlayerRepo = teamPlayerRepo;
        this.teamRepo = teamRepo;
        this.gameWeekRepo = gameWeekRepo;
    }

    public void updatePricesForGameWeek(Long gameWeekId) {
        List<BasketballPlayer> activePlayers = playerRepo.findByActiveTrue();
        
        for (BasketballPlayer player : activePlayers) {
            updatePlayerPrice(player, gameWeekId);
        }
    }

    private void updatePlayerPrice(BasketballPlayer player, Long gameWeekId) {
        BigDecimal currentPrice = player.getMarketValue();
        BigDecimal newPrice = calculateNewPrice(player, gameWeekId);
        
        // Only update if price changed significantly (more than 0.1)
        BigDecimal priceDifference = newPrice.subtract(currentPrice).abs();
        if (priceDifference.compareTo(new BigDecimal("0.1")) >= 0) {
            
            // Update player's market value
            player.setMarketValue(newPrice);
            playerRepo.save(player);
            
            // Record price change history
            recordPriceChange(player, gameWeekId, currentPrice, newPrice);
        }
    }

    private BigDecimal calculateNewPrice(BasketballPlayer player, Long gameWeekId) {
        BigDecimal currentPrice = player.getMarketValue();
        
        // Calculate performance score for recent matches
        double performanceScore = calculatePerformanceScore(player, gameWeekId);
        
        // Calculate ownership percentage
        double ownershipPercentage = calculateOwnershipPercentage(player);
        
        // Determine price multiplier based on performance and ownership
        double multiplier = calculatePriceMultiplier(performanceScore, ownershipPercentage);
        
        // Apply multiplier with maximum change limit
        BigDecimal priceChange = currentPrice.multiply(new BigDecimal(multiplier - 1.0));
        BigDecimal maxChange = currentPrice.multiply(PRICE_CHANGE_FACTOR);
        
        // Limit price change to maximum allowed
        if (priceChange.abs().compareTo(maxChange) > 0) {
            priceChange = priceChange.signum() >= 0 ? maxChange : maxChange.negate();
        }
        
        BigDecimal newPrice = currentPrice.add(priceChange);
        
        // Ensure price stays within bounds
        if (newPrice.compareTo(MIN_PRICE) < 0) {
            newPrice = MIN_PRICE;
        } else if (newPrice.compareTo(MAX_PRICE) > 0) {
            newPrice = MAX_PRICE;
        }
        
        return newPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private double calculatePerformanceScore(BasketballPlayer player, Long gameWeekId) {
        // Get recent performances (last 3 gameweeks)
        List<PlayerPerformance> recentPerformances = performanceRepo.findRecentPerformancesByPlayer(player.getId(), 3);
        
        if (recentPerformances.isEmpty()) {
            return 15.0; // Average score if no data
        }
        
        double totalScore = recentPerformances.stream()
                .mapToDouble(this::calculateFantasyPoints)
                .sum();
        
        return totalScore / recentPerformances.size();
    }

    private double calculateFantasyPoints(PlayerPerformance perf) {
        double points = 0.0;
        if (perf.getPoints() != null) points += perf.getPoints() * 1.0;
        if (perf.getRebounds() != null) points += perf.getRebounds() * 1.2;
        if (perf.getAssists() != null) points += perf.getAssists() * 1.5;
        if (perf.getSteals() != null) points += perf.getSteals() * 3.0;
        if (perf.getBlocks() != null) points += perf.getBlocks() * 3.0;
        if (perf.getTurnovers() != null) points -= perf.getTurnovers() * 1.0;
        return points;
    }

    private double calculateOwnershipPercentage(BasketballPlayer player) {
        long totalTeams = Math.max(1, teamRepo.count());
        long teamsOwning = teamPlayerRepo.countActiveTeamsByPlayerId(player.getId());
        return (teamsOwning * 100.0) / totalTeams;
    }

    private double calculatePriceMultiplier(double performanceScore, double ownershipPercentage) {
        double multiplier = 1.0;
        
        // Performance factor
        if (performanceScore >= HIGH_PERFORMANCE_THRESHOLD) {
            multiplier += 0.05; // 5% increase for high performance
        } else if (performanceScore <= LOW_PERFORMANCE_THRESHOLD) {
            multiplier -= 0.05; // 5% decrease for low performance
        }
        
        // Ownership factor
        if (ownershipPercentage >= HIGH_OWNERSHIP_THRESHOLD) {
            multiplier += 0.03; // 3% increase for high ownership
        } else if (ownershipPercentage <= LOW_OWNERSHIP_THRESHOLD) {
            multiplier -= 0.02; // 2% decrease for low ownership
        }
        
        return multiplier;
    }

    private void recordPriceChange(BasketballPlayer player, Long gameWeekId, BigDecimal oldPrice, BigDecimal newPrice) {
        double ownershipPercentage = calculateOwnershipPercentage(player);
        double performanceScore = calculatePerformanceScore(player, gameWeekId);
        
        String reason = buildPriceChangeReason(performanceScore, ownershipPercentage);
        
        GameWeek gameWeek = gameWeekRepo.findById(gameWeekId)
                .orElseThrow(() -> new RuntimeException("GameWeek not found: " + gameWeekId));
        
        PlayerPriceHistory history = PlayerPriceHistory.builder()
                .player(player)
                .gameWeek(gameWeek)
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .priceChange(newPrice.subtract(oldPrice))
                .ownershipPercentage(BigDecimal.valueOf(ownershipPercentage))
                .performanceScore(BigDecimal.valueOf(performanceScore))
                .reason(reason)
                .build();
        
        priceHistoryRepo.save(history);
    }

    private String buildPriceChangeReason(double performanceScore, double ownershipPercentage) {
        StringBuilder reason = new StringBuilder();
        
        if (performanceScore >= HIGH_PERFORMANCE_THRESHOLD) {
            reason.append("High performance (").append(String.format("%.1f", performanceScore)).append(" avg points). ");
        } else if (performanceScore <= LOW_PERFORMANCE_THRESHOLD) {
            reason.append("Low performance (").append(String.format("%.1f", performanceScore)).append(" avg points). ");
        }
        
        if (ownershipPercentage >= HIGH_OWNERSHIP_THRESHOLD) {
            reason.append("High ownership (").append(String.format("%.1f", ownershipPercentage)).append("%). ");
        } else if (ownershipPercentage <= LOW_OWNERSHIP_THRESHOLD) {
            reason.append("Low ownership (").append(String.format("%.1f", ownershipPercentage)).append("%). ");
        }
        
        return reason.length() > 0 ? reason.toString().trim() : "Regular market adjustment";
    }
}