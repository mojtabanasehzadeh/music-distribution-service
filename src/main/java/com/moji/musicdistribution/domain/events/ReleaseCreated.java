package com.moji.musicdistribution.domain.events;

import lombok.Getter;

import java.util.UUID;

/**
 * Event that occurs when a new release is created
 */
@Getter
public class ReleaseCreated extends BaseDomainEvent {
    private final String title;
    private final UUID artistId;

    /**
     * Create a new ReleaseCreated event
     */
    public ReleaseCreated(UUID releaseId, String title, UUID artistId) {
        super(releaseId);
        this.title = title;
        this.artistId = artistId;
    }
}