package com.moji.musicdistribution.query.projections;

import com.moji.musicdistribution.domain.events.StreamMonetized;
import com.moji.musicdistribution.domain.events.StreamRecorded;
import com.moji.musicdistribution.query.readmodels.StreamStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Projection that maintains statistics about streams
 * Supports  tracking stream data
 */
@Component
@RequiredArgsConstructor
public class StreamStatsProjection {

    // Map of statistics by song ID
    private final Map<UUID, StreamStatistics> statsBySong = new ConcurrentHashMap<>();

    // Map of statistics by artist ID
    private final Map<UUID, StreamStatistics> statsByArtist = new ConcurrentHashMap<>();

    // Map to track daily streams
    private final Map<String, Map<UUID, Integer>> dailyStreamsBySong = new ConcurrentHashMap<>();

    /**
     * Listen for StreamRecorded events to update statistics
     */
    @EventListener
    public void on(StreamRecorded event) {
        // Update song stats
        updateSongStats(event.getSongId(), event.isMonetizable());

        // Update artist stats
        updateArtistStats(event.getArtistId(), event.isMonetizable());

        // Update daily stats
        updateDailyStats(event.getSongId(), event.getStreamTimestamp());
    }

    /**
     * Listen for StreamMonetized events to update monetization statistics
     */
    @EventListener
    public void on(StreamMonetized event) {
        // Additional monetization-specific stats could be tracked here
    }

    /**
     * Update statistics for a song
     */
    private void updateSongStats(UUID songId, boolean monetizable) {
        StreamStatistics stats = statsBySong.computeIfAbsent(
                songId,
                id -> new StreamStatistics(id)
        );

        stats.incrementTotalStreams();
        if (monetizable) {
            stats.incrementMonetizedStreams();
        } else {
            stats.incrementNonMonetizedStreams();
        }
    }

    /**
     * Update statistics for an artist
     */
    private void updateArtistStats(UUID artistId, boolean monetizable) {
        StreamStatistics stats = statsByArtist.computeIfAbsent(
                artistId,
                id -> new StreamStatistics(id)
        );

        stats.incrementTotalStreams();
        if (monetizable) {
            stats.incrementMonetizedStreams();
        } else {
            stats.incrementNonMonetizedStreams();
        }
    }

    /**
     * Update daily stream statistics
     */
    private void updateDailyStats(UUID songId, Instant timestamp) {
        // Format the date as YYYY-MM-DD
        String dateKey = timestamp.truncatedTo(ChronoUnit.DAYS).toString().substring(0, 10);

        // Get or create the map for this day
        Map<UUID, Integer> dailyStreams = dailyStreamsBySong.computeIfAbsent(
                dateKey,
                date -> new HashMap<>()
        );

        // Increment the stream count for this song
        dailyStreams.put(songId, dailyStreams.getOrDefault(songId, 0) + 1);
    }

    /**
     * Get statistics for a song
     */
    public StreamStatistics getSongStatistics(UUID songId) {
        return statsBySong.getOrDefault(songId, new StreamStatistics(songId));
    }

    /**
     * Get statistics for an artist
     */
    public StreamStatistics getArtistStatistics(UUID artistId) {
        return statsByArtist.getOrDefault(artistId, new StreamStatistics(artistId));
    }

    /**
     * Get the number of daily streams for a song on a specific date
     */
    public int getDailyStreamsForSong(UUID songId, String date) {
        Map<UUID, Integer> dailyStreams = dailyStreamsBySong.getOrDefault(date, new HashMap<>());
        return dailyStreams.getOrDefault(songId, 0);
    }
}