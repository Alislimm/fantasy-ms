package com.example.fantasy.repository;

import com.example.fantasy.domain.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
    Optional<ScoringRule> findByMetric(String metric);
}
