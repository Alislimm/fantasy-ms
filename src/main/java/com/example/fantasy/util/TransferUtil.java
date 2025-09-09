package com.example.fantasy.util;

public class TransferUtil {
    public static final int FREE_TRANSFERS_PER_WEEK = 1;
    public static final int TRANSFER_PENALTY_POINTS = 10; // per extra transfer

    public static int calculateTransferPenalty(long transfersThisWeek) {
        long extras = Math.max(0, transfersThisWeek - FREE_TRANSFERS_PER_WEEK);
        return (int) (extras * TRANSFER_PENALTY_POINTS);
    }
}
