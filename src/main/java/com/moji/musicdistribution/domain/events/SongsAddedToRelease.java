package com.moji.musicdistribution.domain.events;

import com.moji.musicdistribution.domain.aggregates.Release;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

/**
 * Event that occurs when songs are added to a release
 */
@Getter
public class SongsAddedToRelease extends BaseDomainEvent {
    private final Set<UUID> songIds;
    private final String releaseTitle;
    private final UUID artistId;

    /**
     * Create a new SongsAddedToRelease event
     */
    public SongsAddedToRelease(UUID releaseId, Set<UUID> songIds, String releaseTitle, UUID artistId) {
        super(releaseId);
        this.songIds = Set.copyOf(songIds); // Immutable copy
        this.releaseTitle = releaseTitle;
        this.artistId = artistId;
    }

    /**
     * Factory method to create event from a release
     */
    public static SongsAddedToRelease fromRelease(Release release, Set<UUID> addedSongIds) {
        return new SongsAddedToRelease(
                release.getId(),
                addedSongIds,
                release.getTitle(),
                release.getArtistId()
        );
    }
}