package com.moji.musicdistribution.domain.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.UUID;

/**
 * Song entity - represents a single song that can be added to releases
 */
@Entity
@Table(name = "songs")
@Getter
@NoArgsConstructor // Required by JPA
public class Song {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "artist_id", nullable = false)
    private UUID artistId;

    @Column(nullable = false)
    private long durationSeconds; // JPA doesn't support Duration directly, so we store seconds

    // Transient field to access the Duration object
    @Transient
    private Duration duration;

    @PostLoad
    private void loadDuration() {
        this.duration = Duration.ofSeconds(this.durationSeconds);
    }

    /**
     * Create a new song
     */
    public Song(UUID id, String title, UUID artistId, Duration duration) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Song title cannot be empty");
        }
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Song duration must be positive");
        }

        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.duration = duration;
        this.durationSeconds = duration.getSeconds();
    }

    /**
     * Get the song duration
     */
    public Duration getDuration() {
        if (duration == null) {
            duration = Duration.ofSeconds(durationSeconds);
        }
        return duration;
    }

    /**
     * Check if this song is by the specified artist
     */
    public boolean isCreatedBy(UUID artistId) {
        return this.artistId.equals(artistId);
    }

    /**
     * Check if this song can be monetized based on its duration
     * A stream of this song needs to be longer than 30 seconds to be monetized
     */
    public boolean isMonetizable(Duration streamDuration) {
        // Only streams longer than 30 seconds are monetized
        return streamDuration.getSeconds() > 30;
    }
}