package com.ice.musicdistribution.domain.events;

import com.ice.musicdistribution.domain.aggregates.Release;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Event that occurs when a label approves a release date
 */
@Getter
public class ReleaseDateApproved extends BaseDomainEvent {
    private final LocalDate approvedDate;
    private final String releaseTitle;
    private final UUID artistId;
    private final UUID labelId;

    /**
     * Create a new ReleaseDateApproved event
     */
    public ReleaseDateApproved(UUID releaseId, LocalDate approvedDate, String releaseTitle,
                               UUID artistId, UUID labelId) {
        super(releaseId);
        this.approvedDate = approvedDate;
        this.releaseTitle = releaseTitle;
        this.artistId = artistId;
        this.labelId = labelId;
    }

    /**
     * Factory method to create event from a release and label
     */
    public static ReleaseDateApproved fromRelease(Release release, UUID labelId) {
        return new ReleaseDateApproved(
                release.getId(),
                release.getApprovedReleaseDate(),
                release.getTitle(),
                release.getArtistId(),
                labelId
        );
    }
}