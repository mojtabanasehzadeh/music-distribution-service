package com.ice.musicdistribution.command.commands;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to propose a release date for a release
 * Corresponds to: artist proposes a release date
 */
@Getter
public class ProposeReleaseDate {
    private final UUID releaseId;
    private final UUID artistId;
    private final LocalDate proposedDate;

    /**
     * Create a new ProposeReleaseDate command
     *
     * @param releaseId    The ID of the release
     * @param artistId     The ID of the artist proposing the date
     * @param proposedDate The proposed release date
     */
    public ProposeReleaseDate(UUID releaseId, UUID artistId, LocalDate proposedDate) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Release ID cannot be null");
        }
        if (artistId == null) {
            throw new IllegalArgumentException("Artist ID cannot be null");
        }
        if (proposedDate == null) {
            throw new IllegalArgumentException("Proposed date cannot be null");
        }
        if (proposedDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Proposed date cannot be in the past");
        }

        this.releaseId = releaseId;
        this.artistId = artistId;
        this.proposedDate = proposedDate;
    }
}