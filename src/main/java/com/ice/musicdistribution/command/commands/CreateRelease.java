package com.ice.musicdistribution.command.commands;

import lombok.Getter;

import java.util.UUID;

/**
 * Command to create a new release
 */
@Getter
public class CreateRelease {
    private final UUID releaseId;
    private final String title;
    private final UUID artistId;

    /**
     * Create a new CreateRelease command
     *
     * @param releaseId The ID for the new release
     * @param title The title of the release
     * @param artistId The ID of the artist who owns the release
     */
    public CreateRelease(UUID releaseId, String title, UUID artistId) {
        if (releaseId == null) {
            throw new IllegalArgumentException("Release ID cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Release title cannot be empty");
        }
        if (artistId == null) {
            throw new IllegalArgumentException("Artist ID cannot be null");
        }

        this.releaseId = releaseId;
        this.title = title;
        this.artistId = artistId;
    }
}