package com.moji.musicdistribution.domain.events;

import com.moji.musicdistribution.domain.aggregates.Release;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

/**
 * Event that occurs when an artist withdraws a release from distribution
 */
@Getter
public class ReleaseWithdrawn extends BaseDomainEvent {
    private final String releaseTitle;
    private final UUID artistId;
    private final Set<UUID> songIds;

    /**
     * Create a new ReleaseWithdrawn event
     */
    public ReleaseWithdrawn(UUID releaseId, String releaseTitle, UUID artistId, Set<UUID> songIds) {
        super(releaseId);
        this.releaseTitle = releaseTitle;
        this.artistId = artistId;
        this.songIds = Set.copyOf(songIds); // Immutable copy
    }

    /**
     * Factory method to create event from a release
     */
    public static ReleaseWithdrawn fromRelease(Release release) {
        return new ReleaseWithdrawn(
                release.getId(),
                release.getTitle(),
                release.getArtistId(),
                release.getSongIds()
        );
    }
}