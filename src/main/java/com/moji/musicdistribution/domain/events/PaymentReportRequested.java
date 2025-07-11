package com.moji.musicdistribution.domain.events;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Event that occurs when an artist requests a payment report for monetized streams
 */
@Getter
public class PaymentReportRequested extends BaseDomainEvent {
    private final UUID artistId;
    private final String artistName;
    private final Instant fromDate;
    private final Instant toDate;
    private final UUID requestId;

    /**
     * Create a new PaymentReportRequested event
     */
    public PaymentReportRequested(UUID requestId, UUID artistId, String artistName,
                                  Instant fromDate, Instant toDate) {
        super(artistId); // Using artistId as the aggregate ID since this event relates to an artist
        this.artistId = artistId;
        this.artistName = artistName;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.requestId = requestId;
    }

    /**
     * Get the period as a formatted string
     */
    public String getReportPeriod() {
        return fromDate.toString() + " to " + toDate.toString();
    }
}