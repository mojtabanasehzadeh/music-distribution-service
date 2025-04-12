package com.ice.musicdistribution.domain.events;

import com.ice.musicdistribution.domain.aggregates.Release;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Event that occurs when an artist proposes a release date
 */
@Getter
public class ReleaseDateProposed extends BaseDomainEvent {
    private final LocalDate proposedDate;
    private final String releaseTitle;
    private final UUID artistId;
    private final UUID labelId;

    /**
     * Create a new ReleaseDateProposed event
     */
    public ReleaseDateProposed(UUID releaseId, LocalDate proposedDate, String releaseTitle,
                               UUID artistId, UUID labelId) {
        super(releaseId);
        this.proposedDate = proposedDate;
        this.releaseTitle = releaseTitle;
        this.artistId = artistId;
        this.labelId = labelId;
    }

    /**
     * Factory method to create event from a release and label
     */
    public static ReleaseDateProposed fromRelease(Release release, UUID labelId) {
        return new ReleaseDateProposed(
                release.getId(),
                release.getProposedReleaseDate(),
                release.getTitle(),
                release.getArtistId(),
                labelId
        );
    }
}