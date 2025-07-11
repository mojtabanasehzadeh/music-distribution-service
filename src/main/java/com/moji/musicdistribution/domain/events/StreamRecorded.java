package com.moji.musicdistribution.domain.events;

import com.moji.musicdistribution.domain.aggregates.Stream;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Event that occurs when a song is streamed
 */
@Getter
public class StreamRecorded extends BaseDomainEvent {
    private final UUID songId;
    private final UUID userId;
    private final Instant streamTimestamp; // Renamed to avoid conflict
    private final Duration duration;
    private final UUID artistId;
    private final String songTitle;

    /**
     * Create a new StreamRecorded event
     */
    public StreamRecorded(UUID streamId, UUID songId, UUID userId, Instant streamTimestamp,
                          Duration duration, UUID artistId, String songTitle) {
        super(streamId);
        this.songId = songId;
        this.userId = userId;
        this.streamTimestamp = streamTimestamp;
        this.duration = duration;
        this.artistId = artistId;
        this.songTitle = songTitle;
    }

    /**
     * Factory method to create event from a stream and additional song info
     */
    public static StreamRecorded fromStream(Stream stream, UUID artistId, String songTitle) {
        return new StreamRecorded(
                stream.getId(),
                stream.getSongId(),
                stream.getUserId(),
                stream.getStreamDate(),
                stream.getDuration(),
                artistId,
                songTitle
        );
    }

    /**
     * Check if this stream is monetizable (longer than 30 seconds)
     */
    public boolean isMonetizable() {
        // only stream longer than 30sec is considered for monetization
        return duration.getSeconds() > 30;
    }
}