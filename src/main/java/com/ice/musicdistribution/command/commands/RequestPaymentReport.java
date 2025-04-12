package com.ice.musicdistribution.command.commands;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to request a payment report for monetized streams
 * Corresponds to: artist can file for payment for all monetized streams since last payment
 */
@Getter
public class RequestPaymentReport {
    private final UUID artistId;
    private final Instant fromDate;
    private final Instant toDate;
    private final UUID requestId;

    /**
     * Create a new RequestPaymentReport command
     *
     * @param artistId  The ID of the artist requesting the report
     * @param fromDate  The start date for the report period
     * @param toDate    The end date for the report period
     * @param requestId Unique ID for this request
     */
    public RequestPaymentReport(UUID artistId, Instant fromDate, Instant toDate, UUID requestId) {
        if (artistId == null) {
            throw new IllegalArgumentException("Artist ID cannot be null");
        }
        if (fromDate == null) {
            throw new IllegalArgumentException("From date cannot be null");
        }
        if (toDate == null) {
            throw new IllegalArgumentException("To date cannot be null");
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }

        this.artistId = artistId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.requestId = requestId;
    }
}