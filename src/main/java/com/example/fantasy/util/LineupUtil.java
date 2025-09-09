package com.example.fantasy.util;

import java.util.List;

public class LineupUtil {
    public static final int STARTERS_COUNT = 5;
    public static final int BENCH_COUNT = 3;

    public static void validateComposition(List<Long> starters, List<Long> bench) {
        if (starters == null || bench == null) {
            throw new IllegalArgumentException("Lineup lists cannot be null");
        }
        if (starters.size() != STARTERS_COUNT) {
            throw new IllegalArgumentException("Starters must be exactly " + STARTERS_COUNT);
        }
        if (bench.size() != BENCH_COUNT) {
            throw new IllegalArgumentException("Bench must be exactly " + BENCH_COUNT);
        }
        // no duplicates between starters and bench
        for (Long s : starters) {
            if (bench.contains(s)) {
                throw new IllegalArgumentException("Player " + s + " cannot be both starter and bench");
            }
        }
    }
}
