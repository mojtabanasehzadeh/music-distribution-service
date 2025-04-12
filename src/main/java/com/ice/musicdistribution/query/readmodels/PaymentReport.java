package com.ice.musicdistribution.query.readmodels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Read model for artist payment reports
 * Fulfills artist can file for payment for all monetized streams
 */
@Getter
@RequiredArgsConstructor
public class PaymentReport {
    private final UUID artistId;
    private final String artistName;
    private final int totalMonetizedStreams;
    private final BigDecimal totalAmount;
    private final Instant fromDate;
    private final Instant toDate;
    private final Instant generatedAt;
    private final UUID reportId;
    private final List<SongPayment> songPayments;

    /**
     * Payment details for an individual song
     */
    @Getter
    @RequiredArgsConstructor
    public static class SongPayment {
        private final UUID songId;
        private final String songTitle;
        private final int monetizedStreams;
        private final BigDecimal amount;
    }
}