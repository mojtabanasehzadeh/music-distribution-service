package com.ice.musicdistribution.query.readmodels;

import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Read model for artist stream reports
 * Fulfills: artist can request a report of streamed songs
 */
@Value
public class ArtistStreamReport {
    UUID artistId; // Fields are automatically private final with @Value
    String artistName;
    int totalStreams;
    int monetizedStreams;
    int nonMonetizedStreams;
    Instant fromDate;
    Instant toDate;
    List<SongStreamStats> songStats;

    // Manual constructor and getters are no longer needed - Lombok generates them

    /**
     * Statistics for an individual song
     */
    @Value // Use Lombok's @Value for the inner class as well
    public static class SongStreamStats {
        UUID songId;
        String songTitle;
        int totalStreams;
        int monetizedStreams;
        int nonMonetizedStreams;

        // Manual constructor and getters are no longer needed - Lombok generates them
    }
}