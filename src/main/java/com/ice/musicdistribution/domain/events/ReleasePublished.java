package com.ice.musicdistribution.domain.events;

import com.ice.musicdistribution.domain.aggregates.Release;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Event that occurs when a release is published and its songs become available for streaming
 */
@Getter
public class ReleasePublished extends BaseDomainEvent {
    private final LocalDate publishedDate;
    private final String releaseTitle;
    private final UUID artistId;
    private final Set<UUID> songIds;

    /**
     * Create a new ReleasePublished event
     */
    public ReleasePublished(UUID releaseId, LocalDate publishedDate, String releaseTitle,
                            UUID artistId, Set<UUID> songIds) {
        super(releaseId);
        this.publishedDate = publishedDate;
        this.releaseTitle = releaseTitle;
        this.artistId = artistId;
        this.songIds = Set.copyOf(songIds); // Immutable copy
    }

    /**
     * Factory method to create event from a release
     */
    public static ReleasePublished fromRelease(Release release) {
        return new ReleasePublished(
                release.getId(),
                release.getPublishedDate(),
                release.getTitle(),
                release.getArtistId(),
                release.getSongIds()
        );
    }
}