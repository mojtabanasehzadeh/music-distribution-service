package com.moji.musicdistribution.domain.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Stream entity - represents a single stream/play of a song
 */
@Entity
@Table(name = "streams")
@Getter
@NoArgsConstructor // Required by JPA
public class Stream {
    @Id
    private UUID id;

    @Column(name = "song_id", nullable = false)
    private UUID songId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "duration_seconds", nullable = false)
    private long durationSeconds; // JPA doesn't support Duration directly, so we store seconds

    @Column(nullable = false)
    private boolean monetized;

    // Transient field to access the Duration object
    @Transient
    private Duration duration;

    @PostLoad
    private void loadDuration() {
        this.duration = Duration.ofSeconds(this.durationSeconds);
    }

    /**
     * Create a new stream record
     */
    public Stream(UUID id, UUID songId, UUID userId, Instant timestamp, Duration duration) {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Stream duration cannot be negative");
        }

        this.id = id;
        this.songId = songId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.durationSeconds = duration.getSeconds();

        // only stream longer than 30sec is considered for monetization
        this.monetized = duration.getSeconds() > 30;
    }

    /**
     * Get the stream duration
     */
    public Duration getDuration() {
        if (duration == null) {
            duration = Duration.ofSeconds(durationSeconds);
        }
        return duration;
    }

    /**
     * Check if this stream is eligible for monetization
     */
    public boolean isMonetizable() {
        return monetized;
    }

    /**
     * Get the date of this stream (for reporting purposes)
     */
    public Instant getStreamDate() {
        return timestamp;
    }
}