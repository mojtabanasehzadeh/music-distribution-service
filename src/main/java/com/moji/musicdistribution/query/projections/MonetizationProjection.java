package com.moji.musicdistribution.query.projections;

import com.moji.musicdistribution.domain.events.StreamMonetized;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Projection that tracks monetization of streams
 * Supports monetization tracking
 */
@Component
@RequiredArgsConstructor
public class MonetizationProjection {

    // Map to track total monetization amounts by artist
    private final Map<UUID, BigDecimal> artistMonetization = new ConcurrentHashMap<>();

    // Map to track total monetization amounts by song
    private final Map<UUID, BigDecimal> songMonetization = new ConcurrentHashMap<>();

    // Map to track monetized streams by artist
    private final Map<UUID, List<MonetizedStreamRecord>> monetizedStreamsByArtist = new ConcurrentHashMap<>();

    /**
     * Listen for StreamMonetized events
     */
    @EventListener
    public void on(StreamMonetized event) {
        // Update artist monetization
        updateArtistMonetization(event.getArtistId(), event.getMonetizationAmount());

        // Update song monetization
        updateSongMonetization(event.getSongId(), event.getMonetizationAmount());

        // Track the monetized stream
        trackMonetizedStream(event);
    }

    /**
     * Update the total monetization amount for an artist
     */
    private void updateArtistMonetization(UUID artistId, BigDecimal amount) {
        artistMonetization.compute(artistId, (id, currentAmount) ->
                currentAmount == null ? amount : currentAmount.add(amount));
    }

    /**
     * Update the total monetization amount for a song
     */
    private void updateSongMonetization(UUID songId, BigDecimal amount) {
        songMonetization.compute(songId, (id, currentAmount) ->
                currentAmount == null ? amount : currentAmount.add(amount));
    }

    /**
     * Track a monetized stream
     */
    private void trackMonetizedStream(StreamMonetized event) {
        MonetizedStreamRecord record = new MonetizedStreamRecord(
                event.getId(),
                event.getSongId(),
                event.getStreamTimestamp(),
                event.getMonetizationAmount()
        );

        // Add to the list of monetized streams for this artist
        List<MonetizedStreamRecord> artistStreams = monetizedStreamsByArtist.computeIfAbsent(
                event.getArtistId(),
                id -> new ArrayList<>()
        );
        artistStreams.add(record);
    }

    /**
     * Get the total monetization amount for an artist
     */
    public BigDecimal getArtistMonetization(UUID artistId) {
        return artistMonetization.getOrDefault(artistId, BigDecimal.ZERO);
    }

    /**
     * Get the total monetization amount for a song
     */
    public BigDecimal getSongMonetization(UUID songId) {
        return songMonetization.getOrDefault(songId, BigDecimal.ZERO);
    }

    /**
     * Get all monetized streams for an artist between two dates
     */
    public List<MonetizedStreamRecord> getArtistMonetizedStreams(UUID artistId, Instant fromDate, Instant toDate) {
        List<MonetizedStreamRecord> allStreams = monetizedStreamsByArtist.getOrDefault(artistId, new ArrayList<>());

        return allStreams.stream()
                .filter(record -> !record.timestamp.isBefore(fromDate) && !record.timestamp.isAfter(toDate))
                .toList();
    }

    /**
     * Record of a monetized stream
     */
    public static class MonetizedStreamRecord {
        private final UUID streamId;
        private final UUID songId;
        private final Instant timestamp;
        private final BigDecimal amount;

        public MonetizedStreamRecord(UUID streamId, UUID songId, Instant timestamp, BigDecimal amount) {
            this.streamId = streamId;
            this.songId = songId;
            this.timestamp = timestamp;
            this.amount = amount;
        }

        public UUID getStreamId() {
            return streamId;
        }

        public UUID getSongId() {
            return songId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}