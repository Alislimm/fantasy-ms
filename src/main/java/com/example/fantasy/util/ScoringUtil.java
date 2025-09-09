package com.example.fantasy.util;

import com.example.fantasy.domain.PlayerPerformance;
import com.example.fantasy.domain.ScoringRule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScoringUtil {

    public static int calculateFantasyPoints(PlayerPerformance p, Map<String, BigDecimal> rules) {
        // Simple default formula if rule missing
        int points = 0;
        points += p.getPoints() != null ? p.getPoints() * get(rules, "POINT", 1) : 0;
        points += p.getRebounds() != null ? p.getRebounds() * get(rules, "REBOUND", 1) : 0;
        points += p.getAssists() != null ? p.getAssists() * get(rules, "ASSIST", 1) : 0;
        points += p.getSteals() != null ? p.getSteals() * get(rules, "STEAL", 3) : 0;
        points += p.getBlocks() != null ? p.getBlocks() * get(rules, "BLOCK", 3) : 0;
        points -= p.getTurnovers() != null ? p.getTurnovers() * get(rules, "TURNOVER", -1) : 0;
        points += p.getThreeMade() != null ? p.getThreeMade() * get(rules, "THREE_MADE", 1) : 0;
        // Could add FG%, FT%, double-doubles, etc.
        return points;
    }

    private static int get(Map<String, BigDecimal> rules, String key, int defaultVal) {
        BigDecimal v = rules.get(key);
        if (v == null) return defaultVal;
        return v.intValue();
    }

    public static Map<String, BigDecimal> toRuleMap(List<ScoringRule> rules) {
        return rules.stream().collect(Collectors.toMap(ScoringRule::getMetric, ScoringRule::getPointsPerUnit));
    }
}
