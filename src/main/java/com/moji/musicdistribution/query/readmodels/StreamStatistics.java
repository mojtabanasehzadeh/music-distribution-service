package com.moji.musicdistribution.query.readmodels;

import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Read model for stream statistics
 * Supports tracking stream data
 */
@Getter
public class StreamStatistics {
    private final UUID id;
    private final AtomicInteger totalStreams = new AtomicInteger(0);
    private final AtomicInteger monetizedStreams = new AtomicInteger(0);
    private final AtomicInteger nonMonetizedStreams = new AtomicInteger(0);

    /**
     * Create a new statistics object for the given ID
     * (could be a song ID or artist ID)
     */
    public StreamStatistics(UUID id) {
        this.id = id;
    }

    /**
     * Increment the total stream count
     */
    public void incrementTotalStreams() {
        totalStreams.incrementAndGet();
    }

    /**
     * Increment the monetized stream count
     */
    public void incrementMonetizedStreams() {
        monetizedStreams.incrementAndGet();
    }

    /**
     * Increment the non-monetized stream count
     */
    public void incrementNonMonetizedStreams() {
        nonMonetizedStreams.incrementAndGet();
    }

    /**
     * Get the percentage of streams that are monetized
     */
    public double getMonetizationRate() {
        int total = totalStreams.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) monetizedStreams.get() / total;
    }
}