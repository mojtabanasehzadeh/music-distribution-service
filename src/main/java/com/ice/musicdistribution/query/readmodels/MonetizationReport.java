package com.ice.musicdistribution.query.readmodels;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Read model for monetization reports
 * Supporting: artist can file for payment for all monetized streams
 */
@Getter
@RequiredArgsConstructor
public class MonetizationReport {
    private final UUID artistId;
    private final String artistName;
    private final int totalStreams;
    private final int monetizableStreams;
    private final BigDecimal estimatedRevenue;
    private final Instant lastPaymentDate;
    private final Instant fromDate;
    private final Instant toDate;
    private final Instant generatedAt;
}