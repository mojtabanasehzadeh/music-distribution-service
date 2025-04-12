package com.ice.musicdistribution.command.commands;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to publish a release on its approved date
 * Corresponds to: only when release date is agreed and reached, songs are distributed
 */
@Getter
public class PublishRelease {
    private final UUID releaseId;
    private final LocalDate currentDate;

    /**
     * Create a new PublishRelease command
     *
     * @param releaseId   The ID of the release to publish
     * @param currentDate The current date (to check against approved date)
     */
    public PublishRelease(UUID releaseId, LocalDate currentDate) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Release ID cannot be null");
        }
        if (currentDate == null) {
            throw new IllegalArgumentException("Current date cannot be null");
        }

        this.releaseId = releaseId;
        this.currentDate = currentDate;
    }
}