package com.moji.musicdistribution.domain.events;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Event that occurs when a stream is monetized (duration > 30 seconds)
 */
@Getter
public class StreamMonetized extends BaseDomainEvent {
    private final UUID songId;
    private final UUID artistId;
    private final Instant streamTimestamp; // Renamed to avoid conflict
    private final Duration duration;
    private final BigDecimal monetizationAmount;  // This would be calculated based on business rules

    /**
     * Create a new StreamMonetized event
     */
    public StreamMonetized(UUID streamId, UUID songId, UUID artistId,
                           Instant streamTimestamp, Duration duration, BigDecimal monetizationAmount) {
        super(streamId);
        this.songId = songId;
        this.artistId = artistId;
        this.streamTimestamp = streamTimestamp;
        this.duration = duration;
        this.monetizationAmount = monetizationAmount;
    }

    /**
     * Factory method to create event from a StreamRecorded event
     */
    public static StreamMonetized fromStreamRecorded(StreamRecorded streamRecorded, BigDecimal amount) {
        if (!streamRecorded.isMonetizable()) {
            throw new IllegalArgumentException("Cannot monetize streams shorter than 30 seconds");
        }

        return new StreamMonetized(
                streamRecorded.getId(),
                streamRecorded.getSongId(),
                streamRecorded.getArtistId(),
                streamRecorded.getStreamTimestamp(),
                streamRecorded.getDuration(),
                amount
        );
    }
}