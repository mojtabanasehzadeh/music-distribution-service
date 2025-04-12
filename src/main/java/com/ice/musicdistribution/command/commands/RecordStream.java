package com.ice.musicdistribution.command.commands;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Command to record a stream of a song
 * keep track of streamed released songs
 */
@Getter
public class RecordStream {
    private final UUID songId;
    private final UUID userId;
    private final Duration duration;
    private final Instant streamTimestamp;

    /**
     * Create a new RecordStream command
     *
     * @param songId          The ID of the song that was streamed
     * @param userId          The ID of the user who streamed the song
     * @param duration        The duration of the stream
     * @param streamTimestamp When the stream occurred
     */
    public RecordStream(UUID songId, UUID userId, Duration duration, Instant streamTimestamp) {
        if (songId == null) {
            throw new IllegalArgumentException("Song ID cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Duration cannot be null or negative");
        }
        if (streamTimestamp == null) {
            throw new IllegalArgumentException("Stream timestamp cannot be null");
        }

        this.songId = songId;
        this.userId = userId;
        this.duration = duration;
        this.streamTimestamp = streamTimestamp;
    }

    /**
     * Check if this stream is monetizable (longer than 30 seconds)
     * only stream longer than 30sec is considered for monetization
     */
    public boolean isMonetizable() {
        return duration.getSeconds() > 30;
    }
}